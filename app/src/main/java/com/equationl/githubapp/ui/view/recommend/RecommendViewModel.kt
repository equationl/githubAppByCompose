package com.equationl.githubapp.ui.view.recommend

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.constant.LanguageFilter
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.service.RepoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val repoService: RepoService
) : ViewModel() {

    var viewStates by mutableStateOf(RecommendState())
        private set

    private val _viewEvents = Channel<RecommendEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RVM", "Request Error: ", throwable)
            _viewEvents.send(RecommendEvent.ShowMsg("错误："+throwable.message))
        }
    }

    fun dispatch(action: RecommendAction) {
        when (action) {
            is RecommendAction.RefreshData -> refreshData()
            is RecommendAction.ChangeLanguage -> changeLanguage(action.languageFilter)
            is RecommendAction.ChangeSinceFilter -> changeSince(action.sinceFilter)
        }
    }

    private fun changeLanguage(languageFilter: LanguageFilter) {
        viewStates = viewStates.copy(languageFilter = languageFilter)
        refreshData()
    }

    private fun changeSince(sinceFilter: RecommendSinceFilter) {
        viewStates = viewStates.copy(sinceFilter = sinceFilter)
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch(exception) {
            viewStates = viewStates.copy(isRefreshing = true)
            val response = repoService.getTrendDataAPI(true, AppConfig.API_TOKEN, viewStates.sinceFilter.requestValue, viewStates.languageFilter.requestValue)
            if (response.isSuccessful) {
                val body = response.body()?.map { ReposConversion.trendToReposUIModel(it) }
                if (body == null) {
                    _viewEvents.trySend(RecommendEvent.ShowMsg("body is null!"))
                    viewStates = viewStates.copy(isRefreshing = false)
                }
                else {
                    viewStates = viewStates.copy(
                        isRefreshing = false,
                        dataList = body
                    )
                }
            }
            else {
                _viewEvents.trySend(RecommendEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
                viewStates = viewStates.copy(isRefreshing = false)
            }
        }
    }
}

data class RecommendState(
    val dataList: List<ReposUIModel> = listOf(),
    val isRefreshing: Boolean = false,
    val sinceFilter: RecommendSinceFilter = RecommendSinceFilter.Daily,
    val languageFilter: LanguageFilter = LanguageFilter.All
)

sealed class RecommendAction {
    object RefreshData : RecommendAction()
    data class ChangeSinceFilter(val sinceFilter: RecommendSinceFilter) : RecommendAction()
    data class ChangeLanguage(val languageFilter: LanguageFilter) : RecommendAction()
}

sealed class RecommendEvent {
    data class ShowMsg(val msg: String): RecommendEvent()
}

enum class RecommendSinceFilter(val showName: String, val requestValue: String) {
    Daily("今日", "daily"),
    Weekly("本周", "weekly"),
    Monthly("本月", "monthly")
}