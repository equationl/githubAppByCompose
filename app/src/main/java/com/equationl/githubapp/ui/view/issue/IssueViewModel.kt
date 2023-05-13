package com.equationl.githubapp.ui.view.issue

import android.content.Context
import android.util.Log
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
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.model.bean.CommentRequestModel
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.model.conversion.IssueConversion
import com.equationl.githubapp.model.paging.IssueCommentsPagingSource
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.service.IssueService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssueViewModel @Inject constructor(
    private val issueService: IssueService
): ViewModel() {
    private var userName: String? = null
    private var repoName: String? = null
    private var issueCommentFlow: Flow<PagingData<IssueUIModel>>? = null

    var viewStates by mutableStateOf(IssueState(issueCommentFlow = issueCommentFlow))
        private set

    private val _viewEvents = Channel<IssueEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RVM", "Request Error: ", throwable)
            _viewEvents.send(IssueEvent.ShowMsg("错误："+throwable.message))
        }
    }


    fun dispatch(action: IssueAction) {
        when (action) {
            is IssueAction.ShowMag -> {
                _viewEvents.trySend(IssueEvent.ShowMsg(action.msg))
            }
            is IssueAction.LoadData -> {
                viewModelScope.launch(exception) {
                    loadData(action.userName, action.repoName, action.issueNumber)
                }
            }

            is IssueAction.OnChangeIssueLockStatus -> onChangeIssueLockStatus(action.locked)
            is IssueAction.OnChangeIssueStatus -> onChangeIssueStatus(action.status)
            is IssueAction.DelComment -> delComment(action.comment)
            is IssueAction.EditIssue -> editIssue(action.title, action.content)
            is IssueAction.AddComment -> addComment(action.content)
            is IssueAction.EditComment -> editComment(action.content, action.id)
            is IssueAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.userName, action.repoName, action.issueNumber)
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, userName: String, repoName: String, issueNumber: Int) {
        val realUrl = CommonUtils.getIssueHtmlUrl(userName, repoName, issueNumber.toString())
        when (pos) {
            0 -> { // 在浏览器中打开
                context.browse(realUrl)
            }
            1 -> { // 复制链接
                context.copy(realUrl)
                _viewEvents.trySend(IssueEvent.ShowMsg("已复制"))
            }
            2 -> { // 分享
                context.share(realUrl)
            }
        }
    }

    private fun delComment(comment: IssueUIModel) {
        viewModelScope.launch(exception) {
            val response = issueService.deleteComment(userName ?: "", repoName ?: "", comment.status)
            if (response.isSuccessful) {
                _viewEvents.trySend(IssueEvent.Refresh)
                _viewEvents.trySend(IssueEvent.ShowMsg("已删除"))
            }
            else {
                _viewEvents.trySend(IssueEvent.ShowMsg("删除失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun editIssue(title: String, content: String) {
        val issue = Issue()
        issue.body = content
        issue.title = title
        editIssue(issue = issue)
    }

    private fun addComment(content: String) {
        viewModelScope.launch(exception) {
            val commentRequestModel = CommentRequestModel()
            commentRequestModel.body = content
            val response = issueService.addComment(userName ?: "", repoName ?: "", viewStates.issueInfo.issueNum, commentRequestModel)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(IssueEvent.ShowMsg("body is null!"))
                }
                else {
                    _viewEvents.trySend(IssueEvent.ShowMsg("回复成功"))
                    _viewEvents.trySend(IssueEvent.Refresh)
                }
            }
            else {
                _viewEvents.trySend(IssueEvent.ShowMsg("添加失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun editComment(content: String, id: String) {
        viewModelScope.launch(exception) {
            val commentRequestModel = CommentRequestModel()
            commentRequestModel.body = content
            val response = issueService.editComment(userName ?: "", repoName ?: "", id, commentRequestModel)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(IssueEvent.ShowMsg("body is null!"))
                }
                else {
                    _viewEvents.trySend(IssueEvent.ShowMsg("编辑成功"))
                    _viewEvents.trySend(IssueEvent.Refresh)
                }
            }
            else {
                _viewEvents.trySend(IssueEvent.ShowMsg("编辑失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun editIssue(issue: Issue) {
        viewModelScope.launch(exception) {
            val response = issueService.editIssue(userName ?: "", repoName ?: "", viewStates.issueInfo.issueNum, issue)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(IssueEvent.ShowMsg("body is null!"))
                }
                else {
                    val newIssueInfo = IssueConversion.issueToIssueUIModel(body)
                    viewStates = viewStates.copy(issueInfo = newIssueInfo)
                }
            }
            else {
                _viewEvents.trySend(IssueEvent.ShowMsg("修改失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun onChangeIssueStatus(status: String) {
        val issue = Issue()
        issue.state = status
        editIssue(issue = issue)
    }

    private fun onChangeIssueLockStatus(locked: Boolean) {
        viewModelScope.launch(exception) {
            val response =
                if (locked)
                    issueService.lockIssue(userName ?: "", repoName ?: "", viewStates.issueInfo.issueNum)
                else
                    issueService.unLockIssue(userName ?: "", repoName ?: "", viewStates.issueInfo.issueNum)

            if (response.isSuccessful) {
                viewStates = viewStates.copy(issueInfo = viewStates.issueInfo.copy(locked = locked))
            }
            else {
                _viewEvents.trySend(IssueEvent.ShowMsg("修改失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private suspend fun loadData(
        userName: String,
        repoName: String,
        issueNumber: Int
    ) {
        this.userName = userName
        this.repoName = repoName

        issueCommentFlow = Pager(
            PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
        ) {
            IssueCommentsPagingSource(userName, repoName, issueNumber, issueService)
        }.flow.cachedIn(viewModelScope)

        val issueInfo = loadIssueInfo(userName, repoName, issueNumber)

        viewStates = viewStates.copy(issueCommentFlow = issueCommentFlow, issueInfo = issueInfo)
    }

    private suspend fun loadIssueInfo(
        userName: String,
        repoName: String,
        issueNumber: Int
    ): IssueUIModel {
        val response = issueService.getIssueInfo(true, userName, repoName, issueNumber)
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                _viewEvents.trySend(IssueEvent.ShowMsg("body is null!"))
            }
            else {
                return IssueConversion.issueToIssueUIModel(body)
            }
        }
        else {
            _viewEvents.trySend(IssueEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
        }

        return IssueUIModel()
    }
}

data class IssueState(
    val issueCommentFlow: Flow<PagingData<IssueUIModel>>?,
    val issueInfo: IssueUIModel = IssueUIModel()
)

sealed class IssueAction {
    data class ShowMag(val msg: String): IssueAction()
    data class LoadData(val userName: String, val repoName: String, val issueNumber: Int): IssueAction()
    data class OnChangeIssueStatus(val status: String): IssueAction()
    data class OnChangeIssueLockStatus(val locked: Boolean): IssueAction()
    data class DelComment(val comment: IssueUIModel): IssueAction()
    data class EditIssue(val title: String, val content: String): IssueAction()
    data class AddComment(val content: String): IssueAction()
    data class EditComment(val content: String, val id: String): IssueAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val repoName: String, val issueNumber: Int): IssueAction()
}

sealed class IssueEvent {
    object Refresh: IssueEvent()
    data class ShowMsg(val msg: String): IssueEvent()
}

enum class EditIssueDialogOperate {
    AddComment,
    EditComment,
    EditIssue
}