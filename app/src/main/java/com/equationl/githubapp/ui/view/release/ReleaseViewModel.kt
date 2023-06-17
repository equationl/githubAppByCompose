package com.equationl.githubapp.ui.view.release

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
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.model.bean.Release
import com.equationl.githubapp.model.conversion.ReleaseConversion
import com.equationl.githubapp.model.paging.ReleasePagingSource
import com.equationl.githubapp.model.ui.ReleaseUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReleaseViewModel @Inject constructor(
    private val repoService: RepoService,
    private val dataBase: CacheDB
): BaseViewModel() {
    var viewStates by mutableStateOf(ReleaseState())
        private set

    fun dispatch(action: ReleaseAction) {
        when (action) {
            is ReleaseAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.userName, action.repoName)
            is ReleaseAction.DownloadFile -> downloadFile(action.context, action.url)
            is ReleaseAction.OnChangeTab -> onChangeTab(action.tab, action.userName, action.repoName)
            is ReleaseAction.SetData -> setData(action.userName, action.repoName)
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, userName: String, repoName: String) {
        val realUrl = CommonUtils.getReleaseHtmlUrl(userName, repoName)
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

    private fun setData(userName: String, repoName: String) {
        if (isInit) return

        viewModelScope.launch(exception) {
            val cacheData = dataBase.cacheDB().queryRepositoryRelease("$userName/$repoName", viewStates.currentTab == ReleaseHeaderTag.Release)
            if (!cacheData.isNullOrEmpty()) {
                val body = cacheData[0].data?.fromJson<List<Release>>()
                if (body != null) {
                    Log.i("el", "refreshData: 使用缓存数据")
                    viewStates = viewStates.copy(cacheReleaseList = body.map { ReleaseConversion.releaseToReleaseUiModel(it) })
                }
            }

            val releaseFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                ReleasePagingSource(repoService, userName, repoName, viewStates.currentTab == ReleaseHeaderTag.Release, dataBase) {
                    viewStates = viewStates.copy(cacheReleaseList = null)
                    isInit = true
                }
            }.flow.cachedIn(viewModelScope)

            viewStates = viewStates.copy(releaseFlow = releaseFlow)
        }
    }

    private fun onChangeTab(tab: ReleaseHeaderTag, userName: String, repoName: String) {
        if (viewStates.currentTab != tab) {
            isInit = false
            viewStates = viewStates.copy(currentTab = tab)
            setData(userName, repoName)
        }
    }

    private fun downloadFile(context: Context, url: String?) {
        if (url.isNullOrEmpty()) {
            _viewEvents.trySend(BaseEvent.ShowMsg("获取下载地址失败"))
        }
        else {
            context.browse(url)
        }
    }
}

data class ReleaseState(
    val releaseFlow: Flow<PagingData<ReleaseUIModel>>? = null,
    val cacheReleaseList: List<ReleaseUIModel>? = null,
    val currentTab: ReleaseHeaderTag = ReleaseHeaderTag.Release
)

sealed class ReleaseAction: BaseAction() {
    data class SetData(val userName: String, val repoName: String): ReleaseAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val userName: String, val repoName: String): ReleaseAction()
    data class OnChangeTab(val tab: ReleaseHeaderTag, val userName: String, val repoName: String): ReleaseAction()
    data class DownloadFile(val context: Context, val url: String?): ReleaseAction()
}

sealed class ReleaseEvent: BaseEvent() {
    data class Goto(val route: String): ReleaseEvent()
}

enum class ReleaseHeaderTag(val showText: String) {
    Release("版本"),
    Tag("标记")
}