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
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.bean.Event
import com.equationl.githubapp.model.bean.RepoPermission
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.paging.RepoDynamicPagingSource
import com.equationl.githubapp.model.ui.EventUIAction
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
            is RepoActionDynamicAction.ClickItem -> clickItem(action.item)
            is RepoActionDynamicAction.SetRepoPermission -> setRepoPermission(action.repoPermission)
        }
    }

    private fun setRepoPermission(permission: RepoPermission?) {
        viewStates = viewStates.copy(repoPermission = permission)
    }

    private fun clickItem(item: EventUIModel) {
        Log.i("el", "clickItem: item = $item")

        when (item.actionType) {
            EventUIAction.Person -> {

            }
            EventUIAction.Repos -> {
                if (item.username.isNotBlank() && item.username == item.owner) { // fork
                    _viewEvents.trySend(RepoActionDynamicEvent.Goto("${Route.REPO_DETAIL}/${item.repositoryName}/${item.owner}"))
                }
            }
            EventUIAction.Issue -> {
                _viewEvents.trySend(RepoActionDynamicEvent.Goto("${Route.ISSUE_DETAIL}/${item.repositoryName}/${item.owner}/${item.IssueNum}/${viewStates.repoPermission?.admin ?: true}"))
            }
            EventUIAction.Push -> {
                if (item.pushSha.size == 1) {
                    viewStates = viewStates.copy(
                        showChoosePushDialog = false,
                        pushShaList = listOf(),
                        pushShaDesList = listOf(),
                        pushUiModel = null,
                    )
                    // TODO 有问题
                    _viewEvents.trySend(RepoActionDynamicEvent.Goto("${Route.PUSH_DETAIL}/${item.repositoryName}/${item.owner}/${item.pushSha[0]}"))
                }
                else {
                    // 有多个 PUSH
                    viewStates = viewStates.copy(
                        showChoosePushDialog = true,
                        pushShaList = item.pushSha,
                        pushShaDesList = item.pushShaDes,
                        pushUiModel = item
                    )
                }
            }
            EventUIAction.Release -> {

            }
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
    val cacheDynamic: List<EventUIModel>? = null,
    val repoPermission: RepoPermission? = null,
    val showChoosePushDialog: Boolean = false,
    val pushShaList: List<String> = listOf(),
    val pushShaDesList: List<String> = listOf(),
    val pushUiModel: EventUIModel? = null
)

sealed class RepoActionDynamicAction: BaseAction() {
    data class ShowMsg(val msg: String): RepoActionDynamicAction()
    data class SetData(val owner: String, val repo: String): RepoActionDynamicAction()
    data class ClickItem(val item: EventUIModel): RepoActionDynamicAction()
    data class SetRepoPermission(val repoPermission: RepoPermission?): RepoActionDynamicAction()
}

sealed class RepoActionDynamicEvent: BaseEvent() {
    data class Goto(val route: String): RepoActionDynamicEvent()
}
