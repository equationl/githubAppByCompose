package com.equationl.githubapp.ui.view.repos.action

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.model.bean.Event
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.paging.RepoDynamicPagingSource
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoActionDynamicViewModel @Inject constructor(
    private val repoService: RepoService,
    private val dataBase: CacheDB
) : BaseViewModel() {

    var viewStates by mutableStateOf(RepoActionDynamicState())
        private set

    fun dispatch(action: RepoActionDynamicAction) {
        when (action) {
            is RepoActionDynamicAction.ShowMsg -> { _viewEvents.trySend(BaseEvent.ShowMsg(action.msg)) }
            is RepoActionDynamicAction.SetData -> setData(action.owner, action.repo)
        }
    }

    private fun setData(owner: String, repo: String) {
        if (isInit) return

        viewModelScope.launch(exception) {
            val cacheData = dataBase.cacheDB().queryRepositoryEvent("$owner/$repo")
            if (!cacheData.isNullOrEmpty()) {
                val body = cacheData[0].data?.fromJson<List<Event>>()
                if (body != null) {
                    Log.i("el", "refreshData: 使用缓存数据")
                    viewStates = viewStates.copy(cacheDynamic = body.map { EventConversion.eventToEventUIModel(it) })
                }
            }

            val dynamicFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                RepoDynamicPagingSource(repoService, owner, repo, dataBase) {
                    viewStates = viewStates.copy(cacheDynamic = null)
                    isInit = true
                }
            }.flow.cachedIn(viewModelScope)

            viewStates = viewStates.copy(dynamicFlow = dynamicFlow)

        }
    }
}

data class RepoActionDynamicState(
    val dynamicFlow: Flow<PagingData<EventUIModel>>? = null,
    val cacheDynamic: List<EventUIModel>? = null
)

sealed class RepoActionDynamicAction: BaseAction() {
    data class ShowMsg(val msg: String): RepoActionDynamicAction()
    data class SetData(val owner: String, val repo: String): RepoActionDynamicAction()
}
