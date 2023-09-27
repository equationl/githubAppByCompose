package com.equationl.githubapp.ui.view.issue

import android.content.Context
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
import com.equationl.githubapp.common.database.DBIssueDetail
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.model.bean.CommentRequestModel
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.conversion.IssueConversion
import com.equationl.githubapp.model.paging.IssueCommentsPagingSource
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.service.IssueService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IssueViewModel @Inject constructor(
    private val issueService: IssueService,
    private val dataBase: CacheDB
): BaseViewModel() {
    private var userName: String? = null
    private var repoName: String? = null
    private var issueCommentFlow: Flow<PagingData<IssueUIModel>>? = null

    var viewStates by mutableStateOf(IssueState(issueCommentFlow = issueCommentFlow))
        private set

    fun dispatch(action: IssueAction) {
        when (action) {
            is IssueAction.ShowMag -> {
                _viewEvents.trySend(BaseEvent.ShowMsg(action.msg))
            }
            is IssueAction.LoadData -> {
                if (isInit) return

                viewModelScope.launch(exception) {
                    val userInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()
                    viewStates = viewStates.copy(loginUser = userInfo?.login)
                }

                viewModelScope.launch(exception) {
                    loadIssueInfo(action.userName, action.repoName, action.issueNumber)
                }
                viewModelScope.launch(exception) {
                    loadCommentData(action.userName, action.repoName, action.issueNumber)
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
                _viewEvents.trySend(BaseEvent.ShowMsg("已复制"))
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
                _viewEvents.trySend(BaseEvent.ShowMsg("已删除"))
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("删除失败：${response.errorBody()?.string()}"))
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
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                }
                else {
                    _viewEvents.trySend(IssueEvent.Refresh)
                    _viewEvents.trySend(BaseEvent.ShowMsg("回复成功"))
                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("添加失败：${response.errorBody()?.string()}"))
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
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                }
                else {
                    _viewEvents.trySend(IssueEvent.Refresh)
                    _viewEvents.trySend(BaseEvent.ShowMsg("编辑成功"))
                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("编辑失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private fun editIssue(issue: Issue) {
        viewModelScope.launch(exception) {
            val response = issueService.editIssue(userName ?: "", repoName ?: "", viewStates.issueInfo.issueNum, issue)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                }
                else {
                    val newIssueInfo = IssueConversion.issueToIssueUIModel(body)
                    viewStates = viewStates.copy(issueInfo = newIssueInfo)
                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("修改失败：${response.errorBody()?.string()}"))
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
                _viewEvents.trySend(BaseEvent.ShowMsg("修改失败：${response.errorBody()?.string()}"))
            }
        }
    }

    private suspend fun loadCommentData(
        userName: String,
        repoName: String,
        issueNumber: Int
    ) {
        this.userName = userName
        this.repoName = repoName

        val cacheData = dataBase.cacheDB().queryIssueComment("$userName/$repoName", issueNumber.toString())
        if (!cacheData.isNullOrEmpty()) {
            val body = cacheData[0].data?.fromJson<List<com.equationl.githubapp.model.bean.IssueEvent>>()
            if (body != null) {
                Log.i("el", "refreshData: 使用缓存数据")
                viewStates = viewStates.copy(cacheCommentList = body.map { IssueConversion.issueEventToIssueUIModel(it) })
            }
        }

        issueCommentFlow = Pager(
            PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
        ) {
            IssueCommentsPagingSource(userName, repoName, issueNumber, issueService, dataBase) {
                viewStates = viewStates.copy(cacheCommentList = null)
                isInit = true
            }
        }.flow.cachedIn(viewModelScope)

        viewStates = viewStates.copy(issueCommentFlow = issueCommentFlow)
    }

    private suspend fun loadIssueInfo(
        userName: String,
        repoName: String,
        issueNumber: Int
    ) {
        val cacheData = dataBase.cacheDB().queryIssueDetail("$userName/$repoName", issueNumber.toString())
        if (!cacheData.isNullOrEmpty()) {
            val body = cacheData[0].data?.fromJson<Issue>()
            if (body != null) {
                Log.i("el", "refreshData: 使用缓存数据")
                viewStates = viewStates.copy(cacheIssueInfo = IssueConversion.issueToIssueUIModel(body))
            }
        }

        val response = issueService.getIssueInfo(true, userName, repoName, issueNumber)
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
            }
            else {
                dataBase.cacheDB().insertIssueDetail(
                    DBIssueDetail(
                        "$userName/$repoName",
                        "$userName/$repoName",
                        issueNumber.toString(),
                        response.body()?.toJson()
                    )
                )

                viewStates = viewStates.copy(issueInfo = IssueConversion.issueToIssueUIModel(body), cacheIssueInfo = null)
            }
        }
        else {
            _viewEvents.trySend(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
        }
    }
}

data class IssueState(
    val issueCommentFlow: Flow<PagingData<IssueUIModel>>?,
    val issueInfo: IssueUIModel = IssueUIModel(),
    val cacheIssueInfo: IssueUIModel? = null,
    val cacheCommentList: List<IssueUIModel>? = null,
    val loginUser: String? = null
)

sealed class IssueAction: BaseAction() {
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

sealed class IssueEvent: BaseEvent() {
    object Refresh: IssueEvent()
}

enum class EditIssueDialogOperate {
    AddComment,
    EditComment,
    EditIssue
}