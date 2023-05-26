package com.equationl.githubapp.ui.view.repos.issue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.VerticalIconText
import com.equationl.githubapp.ui.common.comPlaceholder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReposIssueContent(
    userName: String,
    reposName: String,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RepoIssueViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
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
            currentState = viewState.currentState,
            onFilterState = { viewModel.dispatch(RepoIssueAction.ChangeState(it)) },
            onSearch = {
                keyboardController?.hide()
                viewModel.dispatch(RepoIssueAction.Search(it))
            }
        )

        val issueList = viewState.issueFlow?.collectAsLazyPagingItems()

        RefreshContent(
            navController = navController,
            pagingItems = issueList,
            cacheList = viewState.cacheIssueList,
            isInit = viewModel.isInit,
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
    pagingItems: LazyPagingItems<IssueUIModel>?,
    cacheList: List<IssueUIModel>?,
    isInit: Boolean,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (commitUIModel: IssueUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    if (pagingItems?.itemCount == 0 && isInit && cacheList.isNullOrEmpty()) {
        return
    }

    BaseRefreshPaging(
        pagingItems = pagingItems,
        cacheItems = cacheList,
        itemUi = {data, isRefresh ->
            Column(modifier = Modifier.padding(8.dp)) {
                ReposIssueItem(
                    data, isRefresh, navController
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

@Composable
private fun ReposIssueHeader(
    currentState: IssueState,
    onFilterState: (newState: IssueState) -> Unit,
    onSearch: (q: String) -> Unit
) {
    var searchValue by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchValue,
            onValueChange = { searchValue = it},
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .clickable {
                            onSearch(searchValue)
                        }
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onAny = { onSearch(searchValue) }),
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        )

        Card(modifier = Modifier.padding(top = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                VerticalIconText(
                    icon = Icons.Outlined.Apps,
                    text = IssueState.All.showName,
                    modifier = Modifier.clickable { onFilterState(IssueState.All) },
                    isPrimary = currentState == IssueState.All
                )
                VerticalIconText(
                    icon = Icons.Outlined.Visibility,
                    text = IssueState.Open.showName,
                    modifier = Modifier.clickable { onFilterState(IssueState.Open) },
                    isPrimary = currentState == IssueState.Open
                )
                VerticalIconText(
                    icon = Icons.Outlined.VisibilityOff,
                    text = IssueState.Close.showName,
                    modifier = Modifier.clickable { onFilterState(IssueState.Close) },
                    isPrimary = currentState == IssueState.Close
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReposIssueItem(
    issueUIModel: IssueUIModel,
    isRefresh: Boolean,
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
                        navHostController = navController,
                        isRefresh = isRefresh
                    )

                    Text(
                        text = issueUIModel.username,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 6.dp).comPlaceholder(isRefresh)
                    )

                }

                Text(text = issueUIModel.time, modifier = Modifier.comPlaceholder(isRefresh))
            }

            Column(modifier = Modifier
                .padding(start = 50.dp)
                .padding(4.dp)) {
                Text(text = issueUIModel.action, modifier = Modifier.comPlaceholder(isRefresh))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        val color = if (issueUIModel.status == "closed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = "Issue status", tint = color, modifier = Modifier.comPlaceholder(isRefresh))
                        Text(text = issueUIModel.status, color = color, modifier = Modifier.comPlaceholder(isRefresh))
                        Text(text = "#${issueUIModel.issueNum}", modifier = Modifier.padding(start = 2.dp).comPlaceholder(isRefresh))
                    }

                    Row {
                        Icon(imageVector = Icons.Filled.ChatBubble, contentDescription = "comment", modifier = Modifier.comPlaceholder(isRefresh))
                        Text(text = issueUIModel.comment, modifier = Modifier.comPlaceholder(isRefresh))
                    }
                }
            }
        }
    }
}