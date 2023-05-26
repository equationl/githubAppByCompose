package com.equationl.githubapp.ui.view.list.generalUser

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
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.conversion.UserConversion
import com.equationl.githubapp.model.paging.UserPagingSource
import com.equationl.githubapp.model.ui.UserUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralUserListViewModel @Inject constructor(
    private val userService: UserService,
    private val repoService: RepoService,
    private val dataBase: CacheDB
): BaseViewModel() {
    var viewStates by mutableStateOf(GeneralUserListState())
        private set

    fun dispatch(action: GeneralUserListAction) {
        when (action) {
            is GeneralUserListAction.SetData -> setData(action.userName, action.repoName, action.requestType)
        }
    }

    private fun setData(userName: String, repoName: String, requestType: GeneralListEnum) {
        if (isInit) return

        viewModelScope.launch(exception) {
            loadDataFromCache(userName, repoName, requestType)

            val userListFlow =
                Pager(
                    PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
                ) {
                    UserPagingSource(userService, repoService, userName, repoName, requestType, dataBase) {
                        viewStates = viewStates.copy(cacheUserList = null)
                        isInit = true
                    }
                }.flow.cachedIn(viewModelScope)
            viewStates = viewStates.copy(userListFlow = userListFlow)
        }
    }

    private suspend fun loadDataFromCache(userName: String, repoName: String, requestType: GeneralListEnum) {
        when (requestType) {
            GeneralListEnum.UserFollower -> {
                val cacheData = dataBase.cacheDB().queryUserFollower(userName)
                if (!cacheData.isNullOrEmpty()) {
                    val body = cacheData[0].data?.fromJson<List<User>>()
                    if (body != null) {
                        Log.i("el", "refreshData: 使用缓存数据")
                        viewStates = viewStates.copy(cacheUserList = body.map { UserConversion.userToUserUIModel(it) })
                    }
                }
            }
            GeneralListEnum.UserFollowed -> {
                val cacheData = dataBase.cacheDB().queryUserFollowed(userName)
                if (!cacheData.isNullOrEmpty()) {
                    val body = cacheData[0].data?.fromJson<List<User>>()
                    if (body != null) {
                        Log.i("el", "refreshData: 使用缓存数据")
                        viewStates = viewStates.copy(cacheUserList = body.map { UserConversion.userToUserUIModel(it) })
                    }
                }
            }
            GeneralListEnum.RepositoryStarUser -> {
                val cacheData = dataBase.cacheDB().queryRepositoryStar("$userName/$repoName")
                if (!cacheData.isNullOrEmpty()) {
                    val body = cacheData[0].data?.fromJson<List<User>>()
                    if (body != null) {
                        Log.i("el", "refreshData: 使用缓存数据")
                        viewStates = viewStates.copy(cacheUserList = body.map { UserConversion.userToUserUIModel(it) })
                    }
                }
            }
            GeneralListEnum.RepositoryWatchUser -> {
                val cacheData = dataBase.cacheDB().queryRepositoryWatcher("$userName/$repoName")
                if (!cacheData.isNullOrEmpty()) {
                    val body = cacheData[0].data?.fromJson<List<User>>()
                    if (body != null) {
                        Log.i("el", "refreshData: 使用缓存数据")
                        viewStates = viewStates.copy(cacheUserList = body.map { UserConversion.userToUserUIModel(it) })
                    }
                }
            }
            GeneralListEnum.OrgMembers -> {
                val cacheData = dataBase.cacheDB().queryOrgMember(userName)
                if (!cacheData.isNullOrEmpty()) {
                    val body = cacheData[0].data?.fromJson<List<User>>()
                    if (body != null) {
                        Log.i("el", "refreshData: 使用缓存数据")
                        viewStates = viewStates.copy(cacheUserList = body.map { UserConversion.userToUserUIModel(it) })
                    }
                }
            }
            else -> { }
        }
    }
}

data class GeneralUserListState(
    val userListFlow: Flow<PagingData<UserUIModel>>? = null,
    val cacheUserList: List<UserUIModel>? = null
)

sealed class GeneralUserListAction: BaseAction() {
    data class SetData(val userName: String, val repoName: String, val requestType: GeneralListEnum): GeneralUserListAction()
}