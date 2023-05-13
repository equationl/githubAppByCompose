package com.equationl.githubapp.ui.view.repos.readme

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.service.RepoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoReadmeViewModel @Inject constructor(
    private val repoService: RepoService,
): ViewModel() {
    var viewStates by mutableStateOf(RepoReadmeState())
        private set

    private val _viewEvents = Channel<RepoReadMeEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RVM", "Request Error: ", throwable)
            _viewEvents.send(RepoReadMeEvent.ShowMsg("错误："+throwable.message))
        }
    }

    fun dispatch(action: RepoReadMeAction) {
        when (action) {
            is RepoReadMeAction.GetReadmeContent -> getReadmeContent(action.repoName, action.ownerName)
        }
    }

    private fun getReadmeContent(repoName: String, ownerName: String) {
        viewModelScope.launch(exception) {
            val response = repoService.getReadmeHtml(true, ownerName, repoName)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(RepoReadMeEvent.ShowMsg("body is null!"))
                }
                else {
                    viewStates = viewStates.copy(
                        readmeContent = body
                    )
                }
            }
            else {
                if (response.code() == 404) {
                    viewStates = viewStates.copy(
                        readmeContent = "该仓库没有 README"
                    )
                }
                else {
                    _viewEvents.trySend(RepoReadMeEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
                }
            }
        }
    }
}

data class RepoReadmeState(
    val readmeContent: String = "Loading..."
)

sealed class RepoReadMeAction {
    data class GetReadmeContent(val repoName: String, val ownerName: String): RepoReadMeAction()
}

sealed class RepoReadMeEvent {
    data class ShowMsg(val msg: String): RepoReadMeEvent()
}