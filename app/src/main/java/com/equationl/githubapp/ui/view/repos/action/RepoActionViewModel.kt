package com.equationl.githubapp.ui.view.repos.action

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBRepositoryDetail
import com.equationl.githubapp.model.bean.Repository
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoActionViewModel @Inject constructor(
    private val repoService: RepoService,
    private val dataBase: CacheDB
): BaseViewModel() {

    var viewStates by mutableStateOf(RepoActionState())
        private set

    fun dispatch(action: RepoActionAction) {
        when (action) {
            is RepoActionAction.ChangeTab -> changeTab(action.toTab)
            is RepoActionAction.GetRepoInfo -> getRepoInfo(action.userName, action.reposName)
        }
    }

    private fun changeTab(toTab: RepoActionTab) {
        viewStates = viewStates.copy(currentTab = toTab)
    }

    private fun getRepoInfo(userName: String, reposName: String) {
        if (isInit) return

        viewModelScope.launch(exception) {
            val cacheData = dataBase.cacheDB().queryRepositoryDetail("$userName/$reposName")
            if (!cacheData.isNullOrEmpty()) {
                val body = cacheData[0].data?.fromJson<Repository>()
                if (body != null) {
                    Log.i("el", "refreshData: 使用缓存数据")
                    viewStates = viewStates.copy(reposUIModel = ReposConversion.reposToReposUIModel(body))
                }
            }

            val response = repoService.getRepoInfo(true, userName, reposName)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                }
                else {
                    dataBase.cacheDB().insertRepositoryDetail(
                        DBRepositoryDetail(
                            "$userName/$reposName",
                            "$userName/$reposName",
                            response.body()?.toJson(),
                            ""
                        )
                    )
                    viewStates = viewStates.copy(
                        reposUIModel = ReposConversion.reposToReposUIModel(body)
                    )
                    isInit = true
                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
            }
        }
    }
}

data class RepoActionState(
    val reposUIModel: ReposUIModel = ReposUIModel(),
    val currentTab: RepoActionTab = RepoActionTab.Dynamic
)

sealed class RepoActionAction: BaseAction() {
    data class ChangeTab(val toTab: RepoActionTab): RepoActionAction()
    data class GetRepoInfo(val userName: String, val reposName: String): RepoActionAction()
}

enum class RepoActionTab {
    Dynamic,
    Commit
}