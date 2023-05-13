package com.equationl.githubapp.ui.view.dynamic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.paging.DynamicPagingSource
import com.equationl.githubapp.model.ui.EventUIAction
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

private const val TAG = "el, DVM"

@HiltViewModel
open class DynamicViewModel @Inject constructor(
    private val userService: UserService,
) : ViewModel() {

    protected open val isGetUserEvent: Boolean  = false

    protected val userInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()

    var viewStates by mutableStateOf(DynamicViewState())
        private set

    protected val _viewEvents = Channel<DynamicViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: DynamicViewAction) {
        when (action) {
            is DynamicViewAction.ShowMsg -> { _viewEvents.trySend(DynamicViewEvent.ShowMsg(action.msg)) }
            is DynamicViewAction.ClickItem -> clickItem(action.eventUIModel)
            is DynamicViewAction.SetData -> setData(action.userName)
        }
    }

    private fun setData(userName: String) {
        val dynamicFlow =
            Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                DynamicPagingSource(userService, userName, isGetUserEvent)
            }.flow.cachedIn(viewModelScope)
        viewStates = viewStates.copy(dynamicFlow = dynamicFlow)
    }

    private fun clickItem(eventUIModel: EventUIModel) {
        when (eventUIModel.actionType) {
            EventUIAction.Person -> {
                _viewEvents.trySend(DynamicViewEvent.Goto("${Route.PERSON_DETAIL}/${eventUIModel.owner}"))
                // PersonActivity.gotoPersonInfo(eventUIModel.owner)
            }
            EventUIAction.Repos -> {
                _viewEvents.trySend(DynamicViewEvent.Goto("${Route.REPO_DETAIL}/${eventUIModel.repositoryName}/${eventUIModel.owner}"))
                // ReposDetailActivity.gotoReposDetail(eventUIModel.owner, eventUIModel.repositoryName)
            }
            EventUIAction.Issue -> {
                _viewEvents.trySend(DynamicViewEvent.Goto("${Route.ISSUE_DETAIL}/${eventUIModel.repositoryName}/${eventUIModel.owner}/${eventUIModel.IssueNum}"))
                // IssueDetailActivity.gotoIssueDetail(eventUIModel.owner, eventUIModel.repositoryName, eventUIModel.IssueNum)
            }
            EventUIAction.Push -> {
                if (eventUIModel.pushSha.size == 1) {
                    viewStates = viewStates.copy(
                        showChoosePushDialog = false,
                        pushShaList = listOf(),
                        pushShaDesList = listOf()
                    )
                    _viewEvents.trySend(DynamicViewEvent.Goto("${Route.PUSH_DETAIL}/${eventUIModel.repositoryName}/${eventUIModel.owner}/${eventUIModel.pushSha[0]}"))
                }
                else {
                    // 有多个 PUSH
                    viewStates = viewStates.copy(
                        showChoosePushDialog = true,
                        pushShaList = eventUIModel.pushSha,
                        pushShaDesList = eventUIModel.pushShaDes
                    )
                }
                /*if (eventUIModel.pushSha.size == 1) {
                    PushDetailActivity.gotoPushDetail(eventUIModel.owner, eventUIModel.repositoryName, eventUIModel.pushSha[0])
                } else {
                    context?.showOptionSelectDialog(eventUIModel.pushShaDes, OnItemClickListener { dialog, _, _, position ->
                        dialog.dismiss()
                        PushDetailActivity.gotoPushDetail(eventUIModel.owner, eventUIModel.repositoryName, eventUIModel.pushSha[position])
                    })
                }*/
            }
            EventUIAction.Release -> {
                _viewEvents.trySend(DynamicViewEvent.ShowMsg("Click A Release Item!"))
            }
        }
    }
}

data class DynamicViewState(
    val dynamicFlow: Flow<PagingData<EventUIModel>>? = null,
    val showChoosePushDialog: Boolean = false,
    val pushShaList: List<String> = listOf(),
    val pushShaDesList: List<String> = listOf()
)

sealed class DynamicViewEvent {
    data class ShowMsg(val msg: String): DynamicViewEvent()
    data class Goto(val route: String): DynamicViewEvent()
}

sealed class DynamicViewAction {
    data class ShowMsg(val msg: String): DynamicViewAction()
    data class ClickItem(val eventUIModel: EventUIModel): DynamicViewAction()
    data class SetData(val userName: String): DynamicViewAction()
}