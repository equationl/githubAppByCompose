package com.equationl.githubapp.ui.view.repos.action

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.service.RepoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoActionViewModel @Inject constructor(
    private val repoService: RepoService
): ViewModel() {

    var viewStates by mutableStateOf(RepoActionState())
        private set

    private val _viewEvents = Channel<RepoActionActionEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RVM", "Request Error: ", throwable)
            _viewEvents.send(RepoActionActionEvent.ShowMsg("错误："+throwable.message))
        }
    }

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
        viewModelScope.launch(exception) {
            val response = repoService.getRepoInfo(true, userName, reposName)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(RepoActionActionEvent.ShowMsg("body is null!"))
                }
                else {
                    viewStates = viewStates.copy(
                        reposUIModel = ReposConversion.reposToReposUIModel(body)
                    )
                }
            }
            else {
                _viewEvents.trySend(RepoActionActionEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
            }
        }
    }
}

data class RepoActionState(
    val reposUIModel: ReposUIModel = ReposUIModel(),
    val currentTab: RepoActionTab = RepoActionTab.Dynamic
)

sealed class RepoActionAction {
    data class ChangeTab(val toTab: RepoActionTab): RepoActionAction()
    data class GetRepoInfo(val userName: String, val reposName: String,): RepoActionAction()
}

sealed class RepoActionActionEvent {
    data class ShowMsg(val msg: String): RepoActionActionEvent()
}

enum class RepoActionTab {
    Dynamic,
    Commit
}