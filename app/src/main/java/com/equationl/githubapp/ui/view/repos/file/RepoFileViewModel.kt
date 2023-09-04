package com.equationl.githubapp.ui.view.repos.file

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.constant.Constant
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.toSplitString
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.FileUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoFileViewModel @Inject constructor(
    private val repoService: RepoService
): BaseViewModel() {

    override val exception= CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RepoFileViewModel", "Request Error: ", throwable)
            _viewEvents.send(BaseEvent.ShowMsg("错误："+throwable.message))
            viewStates = viewStates.copy(isRefresh = false)
        }
    }

    var viewStates by mutableStateOf(RepoFileState())
        private set

    fun dispatch(action: RepoFileAction) {
        when (action) {
            is RepoFileAction.LoadData -> loadData(action.repoName, action.userName, action.branch)
            is RepoFileAction.OnClickFile -> onClickFile(action.fileUIModel, action.userName, action.repoName, action.branch)
            is RepoFileAction.OnClickPath -> onClickPath(action.pos)
        }
    }

    private fun loadData(repoName: String, userName: String, branch: String?) {
        var path = viewStates.pathList.toSplitString()
        path = if (path == "/.") "" else path
        viewStates = viewStates.copy(isRefresh = true)
        viewModelScope.launch(exception) {
            val response = repoService.getRepoFiles(userName, repoName, path, branch = branch)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("body is null!"))
                    viewStates = viewStates.copy(isRefresh = false)
                }
                else {
                    val fileList = ReposConversion.fileListToFileUIList(body)
                    viewStates = viewStates.copy(
                        isRefresh = false,
                        fileList = fileList
                    )
                }
            }
            else {
                _viewEvents.send(BaseEvent.ShowMsg("获取失败：${response.errorBody()?.string()}"))
                viewStates = viewStates.copy(isRefresh = false)
            }
        }
    }

    private fun onClickFile(fileUIModel: FileUIModel, userName: String, repoName: String, branch: String) {
        if (viewStates.isRefresh) return

        if (fileUIModel.type == "file") {
            when (CommonUtils.getFileType(fileUIModel.title)) {
                CommonUtils.FileType.Img -> {
                    val dir = viewStates.pathList.toSplitString()
                    val path = if (dir == "/.") fileUIModel.title else dir + "/" + fileUIModel.title
                    val url = CommonUtils.getFileHtmlUrl(userName, repoName, path, branch) + "?raw=true"
                    val routePath = "${Route.IMAGE_PREVIEW}/${Uri.encode(url)}"
                    _viewEvents.trySend(RepoFileEvent.GoTo(routePath))
                }
                CommonUtils.FileType.Md -> {
                    var dir = viewStates.pathList.toSplitString()
                    dir = if (dir == "/.") "" else "$dir/"
                    val path = Uri.encode(dir + fileUIModel.title)
                    val routePath = "${Route.CODE_DETAIL}/${repoName}/${userName}/$path/${Constant.MdFilePreview}/${path}/$branch"
                    _viewEvents.trySend(RepoFileEvent.GoTo(routePath))
                }
                CommonUtils.FileType.Other -> {
                    var dir = viewStates.pathList.toSplitString()
                    dir = if (dir == "/.") "" else "$dir/"
                    val path = Uri.encode(dir + fileUIModel.title)
                    val routePath = "${Route.CODE_DETAIL}/${repoName}/${userName}/$path/${Constant.RouteParNull}/${path}/$branch"
                    _viewEvents.trySend(RepoFileEvent.GoTo(routePath))
                }
            }
        }
        else {
            val newPathList = mutableListOf<String>()
            newPathList.addAll(viewStates.pathList)
            newPathList.add(fileUIModel.title)
            viewStates = viewStates.copy(
                pathList = newPathList
            )
            _viewEvents.trySend(RepoFileEvent.Refresh)
        }
    }

    private fun onClickPath(pos: Int) {
        if (viewStates.isRefresh) return

        viewStates = if (pos == 0) {
            viewStates.copy(pathList = listOf("."))
        } else {
            val newList = mutableListOf<String>()
            newList.addAll(viewStates.pathList.subList(0, pos + 1))
            viewStates.copy(pathList = newList)
        }

        _viewEvents.trySend(RepoFileEvent.Refresh)
    }
}

data class RepoFileState(
    val isRefresh: Boolean = false,
    val pathList: List<String> = listOf("."),
    val fileList: List<FileUIModel> = listOf()
)

sealed class RepoFileAction : BaseAction() {
    data class LoadData(val repoName: String, val userName: String, val branch: String?): RepoFileAction()
    data class OnClickFile(val fileUIModel: FileUIModel, val userName: String, val repoName: String, val branch: String): RepoFileAction()
    data class OnClickPath(val pos: Int): RepoFileAction()
}

sealed class RepoFileEvent: BaseEvent() {
    object Refresh: RepoFileEvent()
    data class GoTo(val path: String): RepoFileEvent()
}