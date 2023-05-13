package com.equationl.githubapp.ui.view.repos.action

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
import com.equationl.githubapp.model.paging.RepoCommitPagingSource
import com.equationl.githubapp.model.ui.CommitUIModel
import com.equationl.githubapp.service.CommitService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class RepoActionCommitViewModel @Inject constructor(
    private val commitService: CommitService
): ViewModel() {

    private var commitFlow: Flow<PagingData<CommitUIModel>>? = null

    var viewStates by mutableStateOf(RepoActionCommitState(commitFlow = commitFlow))
        private set

    private val _viewEvents = Channel<RepoActionCommitEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoActionCommitAction) {
        when (action) {
            is RepoActionCommitAction.ShowMsg -> { _viewEvents.trySend(RepoActionCommitEvent.ShowMsg(action.msg)) }
            is RepoActionCommitAction.SetData -> setData(action.owner, action.repo)
        }
    }

    private fun setData(owner: String, repo: String) {
        commitFlow = Pager(
            PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
        ) {
            RepoCommitPagingSource(commitService, owner, repo)
        }.flow.cachedIn(viewModelScope)

        viewStates = viewStates.copy(commitFlow = commitFlow)
    }
}

data class RepoActionCommitState(
    val commitFlow: Flow<PagingData<CommitUIModel>>? = null
)

sealed class RepoActionCommitAction {
    data class ShowMsg(val msg: String): RepoActionCommitAction()
    data class SetData(val owner: String, val repo: String): RepoActionCommitAction()
}

sealed class RepoActionCommitEvent {
    data class ShowMsg(val msg: String): RepoActionCommitEvent()
}