package com.equationl.githubapp.ui.view.list.generalRepo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.paging.RepoPagingSource
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.list.GeneralRepoListSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GeneralRepoListViewModel @Inject constructor(
    private val repoService: RepoService
): BaseViewModel() {
    var viewStates by mutableStateOf(GeneralRepoListState())
        private set

    fun dispatch(action: GeneralRepoListAction) {
        when (action) {
            is GeneralRepoListAction.SetData -> setData(action.userName, action.repoName, action.requestType, action.sort)
        }
    }

    private fun setData(userName: String, repoName: String, requestType: GeneralListEnum, sort: GeneralRepoListSort?) {
        val repoListFlow =
            Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                RepoPagingSource(repoService, userName, repoName, sort, requestType)
            }.flow.cachedIn(viewModelScope)
        viewStates = viewStates.copy(repoListFlow = repoListFlow, sort = sort)
    }
}

data class GeneralRepoListState(
    val repoListFlow: Flow<PagingData<ReposUIModel>>? = null,
    val sort: GeneralRepoListSort? = null
)

sealed class GeneralRepoListAction: BaseAction() {
    data class SetData(val userName: String, val repoName: String, val requestType: GeneralListEnum, val sort: GeneralRepoListSort? = null): GeneralRepoListAction()
}

sealed class GeneralRepoListEvent: BaseEvent() {

}