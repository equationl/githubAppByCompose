package com.equationl.githubapp.ui.view.release

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.model.ui.ReleaseUIModel
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.ExpandableItem
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.common.comPlaceholder
import com.equationl.githubapp.ui.view.repos.ReposViewAction
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseScreen(
    repoName: String?,
    repoOwner: String?,
    navController: NavHostController,
    viewModel: ReleaseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is ReleaseEvent.Goto -> {
                    navController.navigate(it.route)
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit){
        viewModel.dispatch(ReleaseAction.SetData(repoOwner ?: "", repoName ?: ""))
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                title = repoName ?: "NULL",
                actions = {
                    IconButton(onClick = { isShowDropMenu = !isShowDropMenu}) {
                        Icon(Icons.Outlined.MoreHoriz, "More")
                    }

                    MoreMenu(
                        isShow = isShowDropMenu,
                        onDismissRequest = { isShowDropMenu = false },
                        onClick =  {
                            viewModel.dispatch(ReposViewAction.ClickMoreMenu(context, it, repoOwner ?: "", repoName ?: ""))
                        }
                    )
                }
            ) {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) { paddingValues ->

        val releaseList = viewState.releaseFlow?.collectAsLazyPagingItems()

        if (releaseList?.itemCount == 0 && viewModel.isInit && viewState.cacheReleaseList.isNullOrEmpty()) {
            return@Scaffold
        }

        Column(modifier = Modifier.padding(paddingValues)) {
            ReleaseContent(
                currentTab = viewState.currentTab,
                pagingItems = releaseList,
                cacheList = viewState.cacheReleaseList,
                onChangeTab = {
                    viewModel.dispatch(ReleaseAction.OnChangeTab(it, repoOwner ?: "", repoName ?: ""))
                },
                onLoadError = {
                    viewModel.dispatch(BaseAction.ShowMag(it))
                },
                onDownloadFile = {
                    viewModel.dispatch(ReleaseAction.DownloadFile(context, it))
                }
            )
        }
    }
}

@Composable
private fun ReleaseContent(
    currentTab: ReleaseHeaderTag,
    pagingItems: LazyPagingItems<ReleaseUIModel>?,
    cacheList: List<ReleaseUIModel>?,
    onChangeTab: (tab: ReleaseHeaderTag) -> Unit,
    onLoadError: (msg: String) -> Unit,
    onDownloadFile: (url: String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            currentTab = currentTab,
            onChangeTab = onChangeTab
        )

        RefreshContent(
            pagingItems = pagingItems,
            cacheList = cacheList,
            onLoadError = onLoadError,
            onDownloadFile = onDownloadFile
        )
    }
}

@Composable
private fun RefreshContent(
    pagingItems: LazyPagingItems<ReleaseUIModel>?,
    cacheList: List<ReleaseUIModel>? = null,
    onLoadError: (msg: String) -> Unit,
    onDownloadFile: (url: String?) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    BaseRefreshPaging(
        pagingItems = pagingItems,
        cacheItems = cacheList,
        itemUi = { data, isRefresh ->
            Column(modifier = Modifier.padding(8.dp)) {
                ReleaseItem(data = data, isRefresh = isRefresh) {
                    onDownloadFile(it)
                }
            }
        },
        onLoadError = onLoadError,
        onClickItem = {},
        headerItem = headerItem,
        onRefresh = onRefresh
    )
}

@Composable
private fun ReleaseItem(
    data: ReleaseUIModel,
    isRefresh: Boolean,
    onDownloadFile: (url: String?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
    ) {
        ExpandableItem(
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = data.title ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.comPlaceholder(isRefresh)
                    )

                    Text(
                        text = data.time ?: "",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.comPlaceholder(isRefresh)
                    )

                }
            },
            subContent = {
                ReleaseDetail(data = data) {url ->
                    onDownloadFile(url)
                }
            },
        )
    }
}

@Composable
private fun ReleaseDetail(
    data: ReleaseUIModel,
    downloadFile: (url: String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Material3RichText(modifier = Modifier.padding(start = 4.dp)) {
            Markdown(content = data.body ?: "")
        }

        Text(
            text = "文件下载：",
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 16.dp, start = 4.dp)
        )

        data.assert.forEach { assert ->
            TextButton(onClick = { downloadFile(assert.downloadLink) }) {
                Text(text = assert.name ?: "null")
            }
        }

        TextButton(onClick = { downloadFile(data.tarDownload) }) {
            Text(text = "Source code(tar.gz)")
        }

        TextButton(onClick = { downloadFile(data.zipDownload) }) {
            Text(text = "Source code(zip)")
        }

    }
}

@Composable
private fun Header(
    currentTab: ReleaseHeaderTag,
    onChangeTab: (tab: ReleaseHeaderTag) -> Unit
) {
    Card(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = {
                onChangeTab(ReleaseHeaderTag.Release)
            }) {
                Text(
                    text = ReleaseHeaderTag.Release.showText,
                    color = if (currentTab == ReleaseHeaderTag.Release) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            TextButton(onClick = {
                onChangeTab(ReleaseHeaderTag.Tag)
            }) {
                Text(
                    text = ReleaseHeaderTag.Tag.showText,
                    color = if (currentTab == ReleaseHeaderTag.Tag) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}