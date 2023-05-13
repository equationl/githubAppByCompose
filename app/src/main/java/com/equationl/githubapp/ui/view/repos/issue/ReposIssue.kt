package com.equationl.githubapp.ui.view.repos.issue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.VerticalIconText
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposIssueContent(
    userName: String,
    reposName: String,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RepoIssueViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is RepoIssueEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }

                is RepoIssueEvent.GoTo -> {
                    navController.navigate(it.path)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoIssueAction.SetDate(userName, reposName))
    }

    Column {
        ReposIssueHeader(
            onFilterState = { viewModel.dispatch(RepoIssueAction.ChangeState(it)) },
            onSearch = { viewModel.dispatch(RepoIssueAction.Search(it)) }
        )

        RefreshContent(
            navController = navController,
            pagingItems = viewState.issueFlow.collectAsLazyPagingItems(),
            onLoadError = {
                viewModel.dispatch(RepoIssueAction.ShowMsg(it))
            },
            onClickItem = {
                viewModel.dispatch(RepoIssueAction.GoIssueDetail(userName, reposName, issueNumber = it.issueNum))
            }
        )
    }

}

@Composable
private fun RefreshContent(
    navController: NavHostController,
    pagingItems: LazyPagingItems<IssueUIModel>,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (commitUIModel: IssueUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (pagingItems.loadState.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    if (pagingItems.itemCount < 1) {
        if (pagingItems.loadState.refresh == LoadState.Loading) {
            Text(text = "正在加载中…")
        }
    }
    else {
        rememberSwipeRefreshState.isRefreshing = (pagingItems.loadState.refresh is LoadState.Loading)

        SwipeRefresh(
            state = rememberSwipeRefreshState,
            onRefresh = {
                pagingItems.refresh()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            DynamicLazyColumn(
                pagingItems,
                navController,
                onClickItem,
                headerItem = headerItem
            )
        }
    }

    if (rememberSwipeRefreshState.isRefreshing) {
        onRefresh?.invoke()
    }

}

@Composable
private fun DynamicLazyColumn(
    pagingItems: LazyPagingItems<IssueUIModel>,
    navController: NavHostController,
    onClickItem: (eventUiModel: IssueUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp)
    ) {
        headerItem?.let { it() }

        itemsIndexed(pagingItems, key = { _, item -> item.issueNum}) { _, item ->
            if (item != null) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ReposIssueItem(
                        item, navController
                    ) {
                        onClickItem(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReposIssueHeader(
    onFilterState: (newState: IssueState) -> Unit,
    onSearch: (q: String) -> Unit
) {
    var searchValue by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(value = searchValue, onValueChange = { searchValue = it }, modifier = Modifier.fillMaxWidth().weight(0.9f) )
            IconButton(onClick = { onSearch(searchValue) }, modifier = Modifier.fillMaxWidth().weight(0.1f)) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }

        Card(modifier = Modifier.padding(top = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                VerticalIconText(icon = Icons.Outlined.Apps, text = IssueState.All.showName, modifier = Modifier.clickable { onFilterState(IssueState.All) })
                VerticalIconText(icon = Icons.Outlined.Visibility, text = IssueState.Open.showName, modifier = Modifier.clickable { onFilterState(IssueState.Open) })
                VerticalIconText(icon = Icons.Outlined.VisibilityOff, text = IssueState.Close.showName, modifier = Modifier.clickable { onFilterState(IssueState.Close) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReposIssueItem(
    issueUIModel: IssueUIModel,
    navController: NavHostController,
    onClickItem: () -> Unit
) {
    Card(onClick = onClickItem) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = issueUIModel.image,
                        size = DpSize(50.dp, 50.dp),
                        userName = issueUIModel.username,
                        navHostController = navController
                    )

                    Text(text = issueUIModel.username, modifier = Modifier.padding(start = 4.dp))

                }

                Text(text = issueUIModel.time)
            }

            Column(modifier = Modifier
                .padding(start = 50.dp)
                .padding(4.dp)) {
                Text(text = issueUIModel.action)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Text(text = issueUIModel.status)
                        Text(text = "#${issueUIModel.issueNum}")
                    }

                    Row {
                        Icon(imageVector = Icons.Filled.Comment, contentDescription = null)
                        Text(text = issueUIModel.comment)
                    }
                }
            }
        }
    }
}