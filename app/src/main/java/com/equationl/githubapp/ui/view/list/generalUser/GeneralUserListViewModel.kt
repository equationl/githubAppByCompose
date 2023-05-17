package com.equationl.githubapp.ui.view.list.generalUser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.paging.UserPagingSource
import com.equationl.githubapp.model.ui.UserUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GeneralUserListViewModel @Inject constructor(
    private val userService: UserService,
    private val repoService: RepoService
): BaseViewModel() {
    var viewStates by mutableStateOf(GeneralUserListState())
        private set

    fun dispatch(action: GeneralUserListAction) {
        when (action) {
            is GeneralUserListAction.SetData -> setData(action.userName, action.repoName, action.requestType)
        }
    }

    private fun setData(userName: String, repoName: String, requestType: GeneralListEnum) {
        val userListFlow =
            Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                UserPagingSource(userService, repoService, userName, repoName, requestType)
            }.flow.cachedIn(viewModelScope)
        viewStates = viewStates.copy(userListFlow = userListFlow)
    }
}

data class GeneralUserListState(
    val userListFlow: Flow<PagingData<UserUIModel>>? = null,
)

sealed class GeneralUserListAction: BaseAction() {
    data class SetData(val userName: String, val repoName: String, val requestType: GeneralListEnum): GeneralUserListAction()
}