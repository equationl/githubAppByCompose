package com.equationl.githubapp.ui.view.repos.issue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.VerticalIconText

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
    BaseRefreshPaging(
        pagingItems = pagingItems,
        itemUi = {
            Column(modifier = Modifier.padding(8.dp)) {
                ReposIssueItem(
                    it, navController
                ) {
                    onClickItem(it)
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
            OutlinedTextField(value = searchValue, onValueChange = { searchValue = it }, modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f) )
            IconButton(onClick = { onSearch(searchValue) }, modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f)) {
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

                    Text(
                        text = issueUIModel.username,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 6.dp)
                    )

                }

                Text(text = issueUIModel.time)
            }

            Column(modifier = Modifier
                .padding(start = 50.dp)
                .padding(4.dp)) {
                Text(text = issueUIModel.action)

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        val color = if (issueUIModel.status == "closed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = "Issue status", tint = color)
                        Text(text = issueUIModel.status, color = color)
                        Text(text = "#${issueUIModel.issueNum}", modifier = Modifier.padding(start = 2.dp))
                    }

                    Row {
                        Icon(imageVector = Icons.Filled.ChatBubble, contentDescription = "comment")
                        Text(text = issueUIModel.comment)
                    }
                }
            }
        }
    }
}