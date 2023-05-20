package com.equationl.githubapp.ui.view.repos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.repos.action.ReposActionContent
import com.equationl.githubapp.ui.view.repos.file.ReposFileContent
import com.equationl.githubapp.ui.view.repos.issue.ReposIssueContent
import com.equationl.githubapp.ui.view.repos.readme.ReposReadmeContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
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
    val createIssueDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    var pagerUserScrollEnabled by remember { mutableStateOf(true) }

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
                // 如果是 README 页面则禁止 pager 滑动，否则会有滑动冲突
                pagerUserScrollEnabled = page != ReposPager.Readme.ordinal
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
                },
                onCreateIssue = {
                    createIssueDialogState.show()
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

            MainContent(pagerState, navController, scaffoldState, repoName, repoOwner, pagerUserScrollEnabled)
        }

        CreateIssueDialog(dialogState = createIssueDialogState, onPostDate = { tittle: String, content: String ->
            viewModel.dispatch(ReposViewAction.CreateIssue(repoOwner ?: "", repoName ?: "", tittle, content))
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    pagerState: PagerState,
    navController: NavHostController,
    scaffoldState: BottomSheetScaffoldState,
    repoName: String?,
    repoOwner: String?,
    userScrollEnabled: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        pageCount = 4,
        state = pagerState,
        userScrollEnabled = userScrollEnabled
    ) { page ->
        when (page) {
            0 -> ReposReadmeContent(userName = repoOwner ?: "", reposName = repoName ?: "", scaffoldState)
            1 -> ReposActionContent(userName = repoOwner ?: "", reposName = repoName ?: "", scaffoldState, navController, onChangePager = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(it.ordinal)
                }
            })
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
    onFork: () -> Unit,
    onCreateIssue: () -> Unit
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        BottomAppBar(
            actions = {
                IconButton(onClick = { onFork() }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Fork")
                }

                IconButton(onClick = { onChangeWatch(!isWatch) }) {
                    Icon(imageVector = if (isWatch) Icons.Filled.Visibility else Icons.Outlined.Visibility, contentDescription = "Watch")
                }

                IconButton(onClick = { onChangeStar(!isStar) }) {
                    Icon(imageVector = if (isStar) Icons.Filled.Star else Icons.Outlined.StarBorder, contentDescription = "Star")
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { onCreateIssue() }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add issue")
                }
            }
        )
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

@Composable
private fun CreateIssueDialog(
    dialogState: MaterialDialogState,
    onPostDate: (tittle: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }



    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {

                Text(text = "新建 ISSUE")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it},
                label = {
                    Text(text = "标题")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(text = "内容（支持 markdown）")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "取消")
                }

                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))

                TextButton(onClick = {
                    dialogState.hide()

                    onPostDate(title, content)
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}
