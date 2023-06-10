package com.equationl.githubapp.ui.view.repos

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.model.bean.Branch
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.service.IssueService
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReposViewModel @Inject constructor(
    private val repoService: RepoService,
    private val issueService: IssueService
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
            is ReposViewAction.CreateIssue -> createIssue(action.userName, action.repoName, action.title, action.content)
            is ReposViewAction.OnChangeBranch -> onChangeBranch(action.branch)
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
            3 -> { // 分支
                viewStates = viewStates.copy(branchList = listOf(
                    Branch(name = "加载中...", isClickAble = false)
                ))
                loadBranchList(userName, repoName)
                _viewEvents.trySend(ReposViewEvent.ShowBranchDialog)
            }
            4 -> { // 版本
                _viewEvents.trySend(ReposViewEvent.Goto("${Route.RELEASE_LIST}/$userName/$repoName"))
            }
        }
    }

    private fun loadBranchList(userName: String, repoName: String) {
        viewModelScope.launch(exception) {
            val response = repoService.getBranches(userName, repoName)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    viewStates = viewStates.copy(branchList = body)
                }
                else {
                    viewStates = viewStates.copy(branchList = listOf(Branch(name = "加载失败", isClickAble = false)))
                    _viewEvents.send(BaseEvent.ShowMsg("分支为空"))
                }
            }
            else {
                viewStates = viewStates.copy(branchList = listOf(Branch(name = "加载失败", isClickAble = false)))
                _viewEvents.send(BaseEvent.ShowMsg("获取分支失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun scrollTo(pager: ReposPager) {
        viewStates = viewStates.copy(
            currentPage = pager
        )
    }

    private fun getRepoState(userName: String, repoName: String) {
        if (isInit) return
        viewModelScope.launch(exception) {
            val starResponse = repoService.checkRepoStarred(userName, repoName)
            val watchResponse = repoService.checkRepoWatched(userName, repoName)
            val isStar = starResponse.code() != 404
            val isWatch = watchResponse.code() != 404

            viewStates = viewStates.copy(isWatch = isWatch, isStar = isStar)

            isInit = true
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

    private fun createIssue(userName: String, repoName: String, title: String, content: String) {
        viewModelScope.launch(exception) {
            val issue = Issue()
            issue.title = title
            issue.body = content
            val response = issueService.createIssue(userName, repoName, issue)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    _viewEvents.trySend(
                        ReposViewEvent.Goto(
                        "${Route.ISSUE_DETAIL}/${repoName}/${userName}/${body.number}"
                    ))
                }
            }
            else {
                _viewEvents.send(BaseEvent.ShowMsg("创建 issue 失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun onChangeBranch(branch: Branch) {
        viewStates = viewStates.copy(branch = branch.name)
    }
}

data class ReposViewState(
    val currentPage: ReposPager = ReposPager.Action,
    val isWatch: Boolean = false,
    val isStar: Boolean = false,
    val branch: String? = null,
    val branchList: List<Branch> = listOf()
)

sealed class ReposViewEvent: BaseEvent() {
    data class Goto(val route: String): ReposViewEvent()
    object ShowBranchDialog: ReposViewEvent()
}

sealed class ReposViewAction: BaseAction() {
    data class GetRepoState(val userName: String, val repoName: String): ReposViewAction()
    data class OnChangeStar(val isStar: Boolean, val userName: String, val repoName: String): ReposViewAction()
    data class OnChangeWatch(val isWatch: Boolean, val userName: String, val repoName: String): ReposViewAction()
    data class ClickFork(val userName: String, val repoName: String): ReposViewAction()
    data class ScrollTo(val pager: ReposPager): ReposViewAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val repoName: String): ReposViewAction()
    data class CreateIssue(val userName: String, val repoName: String, val title: String, val content: String): ReposViewAction()
    data class OnChangeBranch(val branch: Branch): ReposViewAction()
}

enum class ReposPager {
    Readme,
    Action,
    File,
    Issue
}