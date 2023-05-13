package com.equationl.githubapp.ui.view.code

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.HtmlUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CodeDetailViewModel @Inject constructor(
    private val repoService: RepoService
): BaseViewModel() {
    var viewStates by mutableStateOf(CodeDetailState())
        private set

    override val exception: CoroutineExceptionHandler
        get() = super.exception.apply {
            viewStates = viewStates.copy(htmlContent = "<h1>该文件不支持预览</h1>")
        }

    public override fun dispatch(action: BaseAction) {
        super.dispatch(action)

        when (action) {
            is CodeDetailAction.LoadDate -> loadData(action.context, action.userName, action.reposName, action.path, action.localCode)
            is CodeDetailAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.userName, action.reposName, action.url)
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, userName: String, reposName: String, url: String) {
        val realUrl = CommonUtils.getFileHtmlUrl(userName, reposName, url)
        when (pos) {
            0 -> { // 在浏览器中打开
                context.browse(realUrl)
            }
            1 -> { // 复制链接
                if (url.isBlank()) return

                context.copy(realUrl)
                _viewEvents.trySend(BaseEvent.ShowMsg("已复制"))
            }
            2 -> { // 分享
                context.share(realUrl)
            }
        }
    }

    private fun loadData(context: Context, userName: String, reposName: String, path: String, localCode: String?) {
        viewModelScope.launch(exception) {
            if (localCode == null || localCode == "null") {
                requestFile(context, userName, reposName, path)
            }
            else {
                val codeContent = File(localCode).readText()
                viewStates = viewStates.copy(htmlContent = codeContent)
            }
        }
    }

    private suspend fun requestFile(context: Context, userName: String, reposName: String, path: String) {
        val response = repoService.getRepoFilesDetail(userName, reposName, path)
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
            }
            else {
                val htmlString = HtmlUtils.resolveHtmlFile(context, body)
                viewStates = viewStates.copy(htmlContent = htmlString)
            }
        }
        else {
            _viewEvents.send(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
        }
    }
}

data class CodeDetailState (
    val htmlContent: String = "loading"
)

sealed class CodeDetailAction: BaseAction() {
    data class LoadDate(val context: Context, val userName: String, val reposName: String, val path: String, val localCode: String?) : CodeDetailAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val reposName: String, val url: String): CodeDetailAction()
}