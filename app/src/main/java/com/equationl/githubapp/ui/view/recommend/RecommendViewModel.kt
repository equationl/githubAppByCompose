package com.equationl.githubapp.ui.view.recommend

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.constant.LanguageFilter
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBTrendRepository
import com.equationl.githubapp.model.bean.TrendingRepoModel
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val repoService: RepoService,
    private val dataBase: CacheDB
) : BaseViewModel() {

    var viewStates by mutableStateOf(RecommendState())
        private set

    override val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RecommendViewModel", "Request Error: ", throwable)
            _viewEvents.send(BaseEvent.ShowMsg("错误："+throwable.message))
            viewStates = viewStates.copy(isRefreshing = false)
        }
    }

    fun dispatch(action: RecommendAction) {
        when (action) {
            is RecommendAction.RefreshData -> refreshData(action.forceRefresh)
            is RecommendAction.ChangeLanguage -> changeLanguage(action.languageFilter)
            is RecommendAction.ChangeSinceFilter -> changeSince(action.sinceFilter)
            RecommendAction.TopOrRefresh -> topOrRefresh()
        }
    }

    private fun changeLanguage(languageFilter: LanguageFilter) {
        viewStates = viewStates.copy(languageFilter = languageFilter)
        refreshData(true)
    }

    private fun changeSince(sinceFilter: RecommendSinceFilter) {
        viewStates = viewStates.copy(sinceFilter = sinceFilter)
        refreshData(true)
    }

    private fun refreshData(forceRefresh: Boolean) {
        if (isInit && !forceRefresh) return

        viewModelScope.launch(exception) {
            viewStates = viewStates.copy(isRefreshing = true)


            val cacheData = dataBase.cacheDB().queryTrend(viewStates.languageFilter.requestValue, viewStates.sinceFilter.requestValue)
            if (!cacheData.isNullOrEmpty()) {
                val body = cacheData[0].data?.fromJson<List<TrendingRepoModel>>()
                if (body != null) {
                    Log.i("el", "refreshData: 使用缓存数据")
                    viewStates = viewStates.copy(cacheDataList = body.map { ReposConversion.trendToReposUIModel(it) })
                }
            }


            val response = repoService.getTrendDataAPI(true, AppConfig.API_TOKEN, viewStates.sinceFilter.requestValue, viewStates.languageFilter.requestValue)
            if (response.isSuccessful) {
                val body = response.body()?.map { ReposConversion.trendToReposUIModel(it) }
                if (body == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                    viewStates = viewStates.copy(isRefreshing = false)
                }
                else {
                    val newCache = DBTrendRepository(
                        key = viewStates.languageFilter.requestValue + viewStates.sinceFilter.requestValue,
                        languageType = viewStates.languageFilter.requestValue,
                        data = response.body()?.toJson(),
                        since = viewStates.sinceFilter.requestValue
                    )
                    dataBase.cacheDB().insertTrend(newCache)

                    viewStates = viewStates.copy(
                        isRefreshing = false,
                        dataList = body,
                        cacheDataList = null
                    )

                    isInit = true
                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
                viewStates = viewStates.copy(isRefreshing = false)
            }
        }
    }

    private fun topOrRefresh() {
        _viewEvents.trySend(RecommendEvent.TopOrRefresh)
    }
}

data class RecommendState(
    val dataList: List<ReposUIModel> = listOf(),
    val cacheDataList: List<ReposUIModel>? = null,
    val isRefreshing: Boolean = false,
    val sinceFilter: RecommendSinceFilter = RecommendSinceFilter.Daily,
    val languageFilter: LanguageFilter = LanguageFilter.All
)

sealed class RecommendAction: BaseAction() {
    object TopOrRefresh: RecommendAction()
    data class RefreshData(val forceRefresh: Boolean) : RecommendAction()
    data class ChangeSinceFilter(val sinceFilter: RecommendSinceFilter) : RecommendAction()
    data class ChangeLanguage(val languageFilter: LanguageFilter) : RecommendAction()
}

sealed class RecommendEvent: BaseEvent() {
    object TopOrRefresh: RecommendEvent()
}

enum class RecommendSinceFilter(val showName: String, val requestValue: String) {
    Daily("今日", "daily"),
    Weekly("本周", "weekly"),
    Monthly("本月", "monthly")
}