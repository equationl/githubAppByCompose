package com.equationl.githubapp.ui.view.repos.issue

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
import com.equationl.githubapp.model.paging.RepoIssuePagingSource
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.service.IssueService
import com.equationl.githubapp.service.SearchService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RepoIssueViewModel @Inject constructor(
    private val issueService: IssueService,
    private val searchService: SearchService
): ViewModel() {
    private val queryFlow = MutableStateFlow(QueryParameter())
    private val issueData = queryFlow.flatMapLatest {
        Pager(
            PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
        ) {
            RepoIssuePagingSource(issueService, searchService, it)
        }.flow.cachedIn(viewModelScope)
    }

    var viewStates by mutableStateOf(RepoIssueState(issueFlow = issueData))
        private set

    private val _viewEvents = Channel<RepoIssueEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoIssueAction) {
        when (action) {
            is RepoIssueAction.SetDate -> setData(userName = action.userName, repoName = action.repoName)
            is RepoIssueAction.ChangeState -> changeState(action.newState)
            is RepoIssueAction.Search -> search(action.q)

            is RepoIssueAction.ShowMsg -> {
                _viewEvents.trySend(RepoIssueEvent.ShowMsg(action.msg))
            }

            is RepoIssueAction.GoIssueDetail -> {
                _viewEvents.trySend(RepoIssueEvent.GoTo(
                    "${Route.ISSUE_DETAIL}/${action.repoName}/${action.userName}/${action.issueNumber}"
                ))
            }
        }
    }


    private fun setData(userName: String, repoName: String) {
        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(
                userName = userName,
                repoName = repoName
            ))
        }
    }

    private fun changeState(newState: IssueState) {
        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(
                state = newState
            ))
        }
    }

    private fun search(search: String) {
        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(
                queryString = search
            ))
        }
    }
}

data class RepoIssueState(
    val issueFlow: Flow<PagingData<IssueUIModel>>
)

sealed class RepoIssueAction {
    data class ChangeState(val newState: IssueState): RepoIssueAction()
    data class Search(val q: String): RepoIssueAction()
    data class SetDate(val userName: String, val repoName: String): RepoIssueAction()
    data class ShowMsg(val msg: String): RepoIssueAction()
    data class GoIssueDetail(val userName: String, val repoName: String, val issueNumber: Int): RepoIssueAction()
}

sealed class RepoIssueEvent {
    data class ShowMsg(val msg: String): RepoIssueEvent()
    data class GoTo(val path: String): RepoIssueEvent()
}

data class QueryParameter(
    val state: IssueState = IssueState.All,
    val queryString: String = "",
    val userName: String = "",
    val repoName: String = ""
)

enum class IssueState(val originalName: String, val showName: String) {
    All("all", "全部"),
    Open("open", "打开"),
    Close("closed", "关闭")
}