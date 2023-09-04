package com.equationl.githubapp.ui.view.code

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.constant.Constant
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.HtmlUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.formatReadme
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

    override val exception: CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("CodeDetailViewModel", "Request Error: ", throwable)
            viewStates = viewStates.copy(contentString = "<h1>该文件不支持预览</h1>")
            _viewEvents.send(BaseEvent.ShowMsg("错误："+throwable.message))
        }
    }

    override fun dispatch(action: BaseAction) {
        super.dispatch(action)

        when (action) {
            is CodeDetailAction.LoadDate -> loadData(action.userName, action.reposName, action.path, action.localCode, action.branch, action.backgroundColor, action.primaryColor)
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

    private fun loadData(userName: String, reposName: String, path: String, localCode: String?, branch: String?, backgroundColor: Color, primaryColor: Color) {
        viewModelScope.launch(exception) {
            if (localCode == null || localCode == Constant.RouteParNull || localCode == Constant.MdFilePreview) {
                requestFile(userName, reposName, path, branch, backgroundColor, primaryColor, localCode)
            }
            else {
                val codeContent = File(localCode).readText()
                viewStates = viewStates.copy(contentString = codeContent)
            }
        }
    }

    private suspend fun requestFile(userName: String, reposName: String, path: String, branch: String?, backgroundColor: Color, primaryColor: Color, localCode: String?) {

        Log.i("el", "requestFile: path = $path")

        val response = repoService.getRepoFilesDetail(userName, reposName, path, ref = branch)

        var resultContent: String

        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                resultContent = "<h1>该文件为空</h1>"
                // viewStates = viewStates.copy(htmlContent = "<h1>该文件为空</h1>")
                _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
            }
            else {
                resultContent = body // HtmlUtils.resolveHtmlFile(body, backgroundColor, primaryColor)
                // viewStates = viewStates.copy(htmlContent = htmlString)
            }
        }
        else {
            resultContent = "<h1>该文件不支持预览</h1>"
            // viewStates = viewStates.copy(htmlContent = "<h1>该文件不支持预览</h1>")
            _viewEvents.send(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
        }

        viewStates = if (localCode == Constant.MdFilePreview) {

            var fullPath = "https://raw.githubusercontent.com/$userName/$reposName"
            if (!branch.isNullOrBlank()) {
                fullPath += "/$branch"
            }
            viewStates.copy(
                contentString = resultContent.formatReadme(fullPath),
                isHtmlContent = false
            )
        } else {
            viewStates.copy(
                contentString = HtmlUtils.resolveHtmlFile(resultContent, backgroundColor, primaryColor),
                isHtmlContent = true
            )
        }
    }
}

data class CodeDetailState (
    var isHtmlContent: Boolean = true,
    val contentString: String? = null
)

sealed class CodeDetailAction: BaseAction() {
    data class LoadDate(val context: Context, val userName: String, val reposName: String, val path: String, val localCode: String?, val branch: String?, val backgroundColor: Color, val primaryColor: Color) : CodeDetailAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val reposName: String, val url: String): CodeDetailAction()
}