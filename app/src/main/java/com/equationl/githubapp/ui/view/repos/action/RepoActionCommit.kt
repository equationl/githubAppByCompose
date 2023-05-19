package com.equationl.githubapp.ui.view.repos.action

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.CommitUIModel
import com.equationl.githubapp.ui.common.BaseRefreshPaging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoActionCommitContent(
    userName: String,
    reposName: String,
    headerItem: LazyListScope.() -> Unit,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RepoActionCommitViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is RepoActionCommitEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoActionCommitAction.SetData(userName, reposName))
    }

    if (viewState.commitFlow != null) {
        val commitList = viewState.commitFlow.collectAsLazyPagingItems()
        RefreshContent(
            commitPagingItems = commitList,
            onLoadError = {
                viewModel.dispatch(RepoActionCommitAction.ShowMsg(it))
            },
            onClickItem = {
                navController.navigate("${Route.PUSH_DETAIL}/$reposName/$userName/${it.sha}")
            },
            headerItem = headerItem
        )
    }
    else {
        Text(text = "Need init...")
    }
}

@Composable
private fun RefreshContent(
    commitPagingItems: LazyPagingItems<CommitUIModel>,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (commitUIModel: CommitUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    BaseRefreshPaging(
        pagingItems = commitPagingItems,
        itemUi = {
            Column(modifier = Modifier.padding(8.dp)) {
                DynamicColumnItem(
                    it
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicColumnItem(
    commitUIModel: CommitUIModel,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = commitUIModel.userName)
                Text(text = commitUIModel.time)
            }

            Text(text = commitUIModel.des, modifier = Modifier.padding(top = 4.dp))
            Text(text = "sha: ${commitUIModel.sha}", modifier = Modifier.padding(top = 4.dp))
        }
    }
}