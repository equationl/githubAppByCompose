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
import com.equationl.githubapp.model.paging.RepoDynamicPagingSource
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.RepoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class RepoActionDynamicViewModel @Inject constructor(
    private val repoService: RepoService,
) : ViewModel() {

    private var dynamicFlow: Flow<PagingData<EventUIModel>>? = null

    var viewStates by mutableStateOf(RepoActionDynamicState(dynamicFlow = dynamicFlow))
        private set

    private val _viewEvents = Channel<RepoActionDynamicEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoActionDynamicAction) {
        when (action) {
            is RepoActionDynamicAction.ShowMsg -> { _viewEvents.trySend(RepoActionDynamicEvent.ShowMsg(action.msg)) }
            is RepoActionDynamicAction.SetData -> setData(action.owner, action.repo)
        }
    }

    private fun setData(owner: String, repo: String) {
        dynamicFlow = Pager(
            PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
        ) {
            RepoDynamicPagingSource(repoService, owner, repo)
        }.flow.cachedIn(viewModelScope)

        viewStates = viewStates.copy(dynamicFlow = dynamicFlow)
    }
}

data class RepoActionDynamicState(
    val dynamicFlow: Flow<PagingData<EventUIModel>>? = null
)

sealed class RepoActionDynamicAction {
    data class ShowMsg(val msg: String): RepoActionDynamicAction()
    data class SetData(val owner: String, val repo: String): RepoActionDynamicAction()
}

sealed class RepoActionDynamicEvent {
    data class ShowMsg(val msg: String): RepoActionDynamicEvent()
}