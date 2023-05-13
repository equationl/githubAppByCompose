package com.equationl.githubapp.ui.view.repos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.repos.action.ReposActionContent
import com.equationl.githubapp.ui.view.repos.file.ReposFileContent
import com.equationl.githubapp.ui.view.repos.issue.ReposIssueContent
import com.equationl.githubapp.ui.view.repos.readme.ReposReadmeContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RepoDetailScreen(
    navController: NavHostController,
    repoName: String?,
    repoOwner: String?,
    viewModel: ReposViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = viewModel.viewStates
    val pagerState = rememberPagerState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
                is ReposViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
            }
        }
    }

    // 监听 pager 变化
    LaunchedEffect(pagerState) {
        viewModel.dispatch(ReposViewAction.GetRepoState(repoOwner ?: "", repoName ?: ""))

        withContext(Dispatchers.IO) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                viewModel.dispatch(ReposViewAction.ScrollTo(ReposPager.values()[page]))
            }
        }
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                 title = repoName ?: "NUll",
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
        bottomBar = {
            BottomBar(
                isStar = viewState.isStar,
                isWatch = viewState.isWatch,
                onChangeStar = {
                    viewModel.dispatch(ReposViewAction.OnChangeStar(it, repoOwner ?: "", repoName ?: ""))
                },
                onChangeWatch = {
                    viewModel.dispatch(ReposViewAction.OnChangeWatch(it, repoOwner ?: "", repoName ?: ""))
                },
                onFork = {
                    viewModel.dispatch(ReposViewAction.ClickFork(repoOwner ?: "", repoName ?: ""))
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }}
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            TabBar(
                viewState,
                onScrollTo = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(it.ordinal)
                    }
                }
            )

            MainContent(pagerState, navController, scaffoldState, repoName, repoOwner)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    pagerState: PagerState,
    navController: NavHostController,
    scaffoldState: BottomSheetScaffoldState,
    repoName: String?,
    repoOwner: String?,
) {
    HorizontalPager(
        pageCount = 4,
        state = pagerState
    ) { page ->
        when (page) {
            0 -> ReposReadmeContent(userName = repoOwner ?: "", reposName = repoName ?: "", scaffoldState)
            1 -> ReposActionContent(userName = repoOwner ?: "", reposName = repoName ?: "", scaffoldState, navController)
            2 -> ReposFileContent(userName = repoOwner ?: "", repoName = repoName ?: "", scaffoldState, navController)
            3 -> ReposIssueContent(userName = repoOwner ?: "", reposName = repoName ?: "", scaffoldState, navController)
        }
    }
}

@Composable
private fun BottomBar(
    isStar: Boolean,
    isWatch: Boolean,
    onChangeStar: (isStar: Boolean) -> Unit,
    onChangeWatch: (isWatch: Boolean) -> Unit,
    onFork: () -> Unit
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        BottomAppBar {
            Spacer(Modifier.weight(0.4f, true))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
            ) {

                BottomItem(text = "fork", icon = Icons.Filled.Share) {
                    onFork()
                }

                BottomItem(
                    text = if (isWatch) "unWatch" else "watch",
                    icon = if (isWatch) Icons.Filled.Visibility else Icons.Outlined.Visibility
                ) {
                    onChangeWatch(!isWatch)
                }

                BottomItem(
                    text = if (isStar) "unStar" else "star",
                    icon = if (isStar) Icons.Filled.Star else Icons.Outlined.StarBorder
                ) {
                    onChangeStar(!isStar)
                }
            }
        }
    }
}

@Composable
private fun BottomItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Text(text = text)
    }
}

@Composable
private fun TabBar(
    viewState: ReposViewState,
    onScrollTo: (to: ReposPager) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        TabItem(
            isSelected = viewState.currentPage == ReposPager.Readme,
            title = "详情信息",
            onScrollTo = { onScrollTo(ReposPager.Readme) }
        )

        TabItem(
            isSelected = viewState.currentPage == ReposPager.Action,
            title = "动态",
            onScrollTo = { onScrollTo(ReposPager.Action) }
        )

        TabItem(
            isSelected = viewState.currentPage == ReposPager.File,
            title = "文件",
            onScrollTo = { onScrollTo(ReposPager.File) }
        )

        TabItem(
            isSelected = viewState.currentPage == ReposPager.Issue,
            title = "Issues",
            onScrollTo = { onScrollTo(ReposPager.Issue) }
        )
    }
}

@Composable
private fun TabItem(
    isSelected: Boolean,
    title: String,
    onScrollTo: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onScrollTo)) {

        Text(title, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified)
    }
}
