package com.equationl.githubapp.ui.view.notify

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.paging.NotifyPagingSource
import com.equationl.githubapp.model.ui.EventUIAction
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.NotificationService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotifyViewModel @Inject constructor(
    private val notifyServer: NotificationService,
): BaseViewModel() {
    var viewStates by mutableStateOf(NotifyState())
        private set

    fun dispatch(action: NotifyAction) {
        when (action) {
            is NotifyAction.ApplyFilter -> applyFilter(action.notifyRequestFilter)
            is NotifyAction.ClickItem -> clickItem(action.uiModel)
            is NotifyAction.ReadAll -> readAll()
        }
    }

    private fun applyFilter(notifyRequestFilter: NotifyRequestFilter) {
        val notifyFlow =
            Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                val all: Boolean?
                val participating: Boolean?
                when (notifyRequestFilter) {
                    NotifyRequestFilter.UnRead -> {
                        all = null
                        participating = null
                    }
                    NotifyRequestFilter.Participate -> {
                        all = false
                        participating = true
                    }
                    NotifyRequestFilter.All -> {
                        all = true
                        participating = false
                    }
                }

                NotifyPagingSource(notifyServer, all, participating)

            }.flow.cachedIn(viewModelScope)

        viewStates = viewStates.copy(notifyFlow = notifyFlow, requestFilter = notifyRequestFilter)
    }

    private fun clickItem(eventUIModel: EventUIModel) {
        setRead(eventUIModel)

        when (eventUIModel.actionType) {
            EventUIAction.Person -> {
                _viewEvents.trySend(NotifyEvent.Goto("${Route.PERSON_DETAIL}/${eventUIModel.owner}"))
            }
            EventUIAction.Repos -> {
                _viewEvents.trySend(NotifyEvent.Goto("${Route.REPO_DETAIL}/${eventUIModel.repositoryName}/${eventUIModel.owner}"))
            }
            EventUIAction.Issue -> {
                _viewEvents.trySend(NotifyEvent.Goto("${Route.ISSUE_DETAIL}/${eventUIModel.repositoryName}/${eventUIModel.owner}/${eventUIModel.IssueNum}"))
            }
            EventUIAction.Push -> {
                if (eventUIModel.pushSha.size == 1) {
                    viewStates = viewStates.copy(
                        showChoosePushDialog = false,
                        pushShaList = listOf(),
                        pushShaDesList = listOf()
                    )
                    _viewEvents.trySend(NotifyEvent.Goto("${Route.PUSH_DETAIL}/${eventUIModel.repositoryName}/${eventUIModel.owner}/${eventUIModel.pushSha[0]}"))
                }
                else {
                    // 有多个 PUSH
                    viewStates = viewStates.copy(
                        showChoosePushDialog = true,
                        pushShaList = eventUIModel.pushSha,
                        pushShaDesList = eventUIModel.pushShaDes
                    )
                }
            }
            EventUIAction.Release -> {
                _viewEvents.trySend(BaseEvent.ShowMsg("Click A Release Item!"))
            }
        }
    }

    private fun setRead(eventUIModel: EventUIModel) {
        // TODO 这里看看标记已读是否需要删除或者刷新
        viewModelScope.launch(exception) {
            val response = notifyServer.setNotificationAsRead(eventUIModel.threadId)
            if (!response.isSuccessful) {
                _viewEvents.trySend(BaseEvent.ShowMsg("标记已读失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun readAll() {
        // TODO 这里看看标记已读是否需要删除或者刷新
        viewModelScope.launch(exception) {
            val response = notifyServer.setAllNotificationAsRead()
            if (!response.isSuccessful) {
                _viewEvents.trySend(BaseEvent.ShowMsg("标记已读失败：${response.errorBody()?.string()}"))
            }
        }
    }
}

data class NotifyState(
    val notifyFlow: Flow<PagingData<EventUIModel>>? = null,
    val requestFilter: NotifyRequestFilter = NotifyRequestFilter.UnRead,
    val showChoosePushDialog: Boolean = false,
    val pushShaList: List<String> = listOf(),
    val pushShaDesList: List<String> = listOf()
)

sealed class NotifyAction: BaseAction() {
    object ReadAll: NotifyAction()
    data class ApplyFilter(val notifyRequestFilter: NotifyRequestFilter): NotifyAction()
    data class ClickItem(val uiModel: EventUIModel): NotifyAction()
}

sealed class NotifyEvent: BaseEvent() {
    data class Goto(val route: String): NotifyEvent()
}

enum class NotifyRequestFilter(val showText: String) {
    UnRead("未读"),
    Participate("参与"),
    All("所有")
}