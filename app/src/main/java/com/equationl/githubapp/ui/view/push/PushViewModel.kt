package com.equationl.githubapp.ui.view.push

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.FileUIModel
import com.equationl.githubapp.model.ui.PushUIModel
import com.equationl.githubapp.service.CommitService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PushViewModel @Inject constructor(
    private val commitService: CommitService
): BaseViewModel() {
    override val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("PushViewModel", "Request Error: ", throwable)
            _viewEvents.send(BaseEvent.ShowMsg("错误："+throwable.message))
            viewStates = viewStates.copy(isRefresh = false)
        }
    }

    var viewStates by mutableStateOf(PushState())
        private set

    fun dispatch(action: PushAction) {
        super.dispatch(action)

        when (action) {
            is PushAction.LoadData -> loadData(action.userName, action.repoName, action.sha, action.backGroundColor, action.primaryColor)
            is PushAction.GoTo -> goTo(action.routePath)
            is PushAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.userName, action.repoName, action.sha)
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, userName: String, repoName: String, sha: String) {
        val realUrl = CommonUtils.getCommitHtmlUrl(userName, repoName, sha)
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

    private fun loadData(userName: String, repoName: String, sha: String, backGroundColor: Color, primaryColor: Color) {
        viewStates = viewStates.copy(isRefresh = true)
        viewModelScope.launch(exception) {
            val response = commitService.getCommitInfo(true, userName, repoName, sha)
            if (response.isSuccessful) {
                val body = response.body()
                viewStates = if (body == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                    viewStates.copy(isRefresh = false)
                } else {
                    val pushInfo = ReposConversion.pushInfoToPushUIModel(body)
                    val fileList = body.files?.map { ReposConversion.repoCommitToFileUIModel(it, backGroundColor, primaryColor) }
                    viewStates.copy(
                        isRefresh = false,
                        pushUIModel = pushInfo,
                        fileUiModel = fileList ?: listOf()
                    )
                }
            }
            else {
                _viewEvents.send(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
                viewStates = viewStates.copy(isRefresh = false)
            }
        }
    }

    private fun goTo(routePath: String) {
        _viewEvents.trySend(PushEvent.Goto(routePath))
    }

}

data class PushState(
    val isRefresh: Boolean = false,
    val pushUIModel: PushUIModel = PushUIModel(),
    val fileUiModel: List<FileUIModel> = listOf()
)

sealed class PushAction: BaseAction() {
    data class LoadData(val userName: String, val repoName: String, val sha: String, val backGroundColor: Color, val primaryColor: Color): PushAction()
    data class GoTo(val routePath: String): PushAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val repoName: String, val sha: String): PushAction()
}

sealed class PushEvent: BaseEvent() {
    data class Goto(val routePath: String): PushEvent()
}