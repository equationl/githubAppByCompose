package com.equationl.githubapp.ui.view.repos

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReposViewModel @Inject constructor(
    val repoService: RepoService,
    // private val dataBase: IssueDb
) : BaseViewModel() {

    var viewStates by mutableStateOf(ReposViewState())
        private set

    fun dispatch(action: ReposViewAction) {
        when (action) {
            is ReposViewAction.ScrollTo -> scrollTo(action.pager)
            is ReposViewAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.userName, action.repoName)
            is ReposViewAction.GetRepoState -> getRepoState(action.userName, action.repoName)
            is ReposViewAction.ClickFork -> clickFork(action.userName, action.repoName)
            is ReposViewAction.OnChangeStar -> onChangeStar(action.isStar, action.userName, action.repoName)
            is ReposViewAction.OnChangeWatch -> onChangeWatch(action.isWatch, action.userName,action.repoName)
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, userName: String, repoName: String) {
        val realUrl = CommonUtils.getReposHtmlUrl(userName, repoName)
        when (pos) {
            0 -> { // 在浏览器中打开
                context.browse(realUrl)
            }
            1 -> { // 复制链接
                context.copy(realUrl)
                _viewEvents.trySend(BaseEvent.ShowMsg("已复制"))
            }
            2 -> { // 分享
                context.share(realUrl)
            }
        }
    }

    private fun scrollTo(pager: ReposPager) {
        viewStates = viewStates.copy(
            currentPage = pager,
            title = "GithubApp",
        )
    }

    private fun getRepoState(userName: String, repoName: String) {
        viewModelScope.launch(exception) {
            val starResponse = repoService.checkRepoStarred(userName, repoName)
            val watchResponse = repoService.checkRepoWatched(userName, repoName)
            val isStar = starResponse.code() != 404
            val isWatch = watchResponse.code() != 404

            viewStates = viewStates.copy(isWatch = isWatch, isStar = isStar)
        }
    }

    private fun onChangeStar(isStar: Boolean, userName: String, repoName: String) {
        viewModelScope.launch(exception) {
            val response = if (isStar) repoService.starRepo(userName, repoName) else repoService.unstarRepo(userName, repoName)
            if (response.isSuccessful) {
                viewStates = viewStates.copy(isStar = isStar)
            }
            else {
                _viewEvents.send(BaseEvent.ShowMsg("更改 Start 状态失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun onChangeWatch(isWatch: Boolean, userName: String, repoName: String) {
        viewModelScope.launch(exception) {
            val response = if (isWatch) repoService.watchRepo(userName, repoName) else repoService.unwatchRepo(userName, repoName)
            if (response.isSuccessful) {
                viewStates = viewStates.copy(isWatch = isWatch)
            }
            else {
                _viewEvents.send(BaseEvent.ShowMsg("更改 Watch 状态失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun clickFork(userName: String, repoName: String) {
        viewModelScope.launch(exception) {
            val response = repoService.createFork(userName, repoName)
            if (response.isSuccessful) {
                _viewEvents.send(BaseEvent.ShowMsg("Fork 成功了"))
            }
            else {
                _viewEvents.send(BaseEvent.ShowMsg("更改 Watch 状态失败：${response.errorBody()?.string()}"))
            }
        }
    }
}

data class ReposViewState(
    val title: String = "TODO LIST",
    val currentPage: ReposPager = ReposPager.Readme,
    val isWatch: Boolean = false,
    val isStar: Boolean = false
)

sealed class ReposViewEvent: BaseEvent() {
    data class Goto(val route: String): ReposViewEvent()
}

sealed class ReposViewAction: BaseAction() {
    data class GetRepoState(val userName: String, val repoName: String): ReposViewAction()
    data class OnChangeStar(val isStar: Boolean, val userName: String, val repoName: String): ReposViewAction()
    data class OnChangeWatch(val isWatch: Boolean, val userName: String, val repoName: String): ReposViewAction()
    data class ClickFork(val userName: String, val repoName: String): ReposViewAction()
    data class ScrollTo(val pager: ReposPager): ReposViewAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val repoName: String): ReposViewAction()
}

enum class ReposPager {
    Readme,
    Action,
    File,
    Issue
}