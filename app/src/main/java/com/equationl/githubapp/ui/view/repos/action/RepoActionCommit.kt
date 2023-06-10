package com.equationl.githubapp.ui.view.repos.action

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.CommitUIModel
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.comPlaceholder
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoActionCommitContent(
    userName: String,
    reposName: String,
    branch: String?,
    headerItem: LazyListScope.() -> Unit,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RepoActionCommitViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    LaunchedEffect(branch) {
        viewModel.dispatch(RepoActionCommitAction.SetData(userName, reposName, branch))
    }

    val commitList = viewState.commitFlow?.collectAsLazyPagingItems()

    if (commitList?.itemCount == 0 && viewModel.isInit && viewState.cacheCommitList.isNullOrEmpty()) {
        return
    }

    RefreshContent(
        commitPagingItems = commitList,
        cacheCommitList = viewState.cacheCommitList,
        onLoadError = {
            viewModel.dispatch(RepoActionCommitAction.ShowMsg(it))
        },
        onClickItem = {
            navController.navigate("${Route.PUSH_DETAIL}/$reposName/$userName/${it.sha}")
        },
        headerItem = headerItem
    )

}

@Composable
private fun RefreshContent(
    commitPagingItems: LazyPagingItems<CommitUIModel>?,
    cacheCommitList: List<CommitUIModel>? = null,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (commitUIModel: CommitUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    BaseRefreshPaging(
        pagingItems = commitPagingItems,
        cacheItems = cacheCommitList,
        itemUi = { data, isRefresh ->
            Column(modifier = Modifier.padding(8.dp)) {
                DynamicColumnItem(
                    data,
                    isRefresh
                ) {
                    onClickItem(data)
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
    isRefresh: Boolean,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = commitUIModel.userName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.comPlaceholder(isRefresh)
                )
                Text(
                    text = commitUIModel.time,
                    modifier = Modifier.comPlaceholder(isRefresh)
                )
            }

            Material3RichText(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .comPlaceholder(isRefresh)
            ) {
                Markdown(content = commitUIModel.des)
            }

            Text(
                text = "sha: ${commitUIModel.sha}",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .comPlaceholder(isRefresh)
            )
        }
    }
}