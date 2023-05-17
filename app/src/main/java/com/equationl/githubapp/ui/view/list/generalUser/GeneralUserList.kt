package com.equationl.githubapp.ui.view.list.generalUser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.UserUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.EmptyItem
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralUserListScreen(
    repoName: String,
    userName: String,
    requestType: GeneralListEnum,
    navHostController: NavHostController,
    viewModel: GeneralUserListViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(GeneralUserListAction.SetData(userName, repoName, requestType))
    }

    Scaffold(
        topBar = {
            val title = remember {
                when (requestType) {
                    GeneralListEnum.UserFollower -> "粉丝"
                    GeneralListEnum.UserFollowed -> "关注"
                    GeneralListEnum.RepositoryStarUser -> "Star"
                    GeneralListEnum.RepositoryWatchUser -> "Watch"
                    else -> "Null"
                }
            }
            TopBar(
                title = title
            ) {
                navHostController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        val userList = viewState.userListFlow?.collectAsLazyPagingItems()

        Column(modifier = Modifier.padding(it)) {
            UserListContent(
                repoPagingItems = userList,
                onLoadError = { msg ->
                    viewModel.dispatch(BaseAction.ShowMag(msg))
                },
                onClickItem = { userUiModel ->
                    navHostController.navigate("${Route.PERSON_DETAIL}/${userUiModel.login}")
                }
            )
        }
    }
}

@Composable
private fun UserListContent(
    repoPagingItems: LazyPagingItems<UserUIModel>?,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (userUiModel: UserUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    UserListRefreshContent(
        repoPagingItems, onLoadError, onClickItem, headerItem, onRefresh
    )
}

@Composable
fun UserListRefreshContent(
    userPagingItems: LazyPagingItems<UserUIModel>?,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (userUiModel: UserUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (userPagingItems?.loadState?.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    rememberSwipeRefreshState.isRefreshing = (userPagingItems?.loadState?.refresh is LoadState.Loading)

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = {
            userPagingItems?.refresh()
            onRefresh?.invoke()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        UserListLazyColumn(
            userPagingItems,
            onClickItem,
            headerItem = headerItem
        )
    }
}

@Composable
private fun UserListLazyColumn(
    repoPagingItems: LazyPagingItems<UserUIModel>?,
    onClickItem: (userUiModel: UserUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp)
    ) {
        headerItem?.let { it() }

        if (repoPagingItems == null) {
            item {
                EmptyItem(true)
            }
        }
        else {
            itemsIndexed(repoPagingItems, key = { _, item -> item.lazyColumnKey}) { _, item ->
                if (item != null) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        UserItem(userUiModel = item) {
                            onClickItem(item)
                        }
                    }
                }
            }

            if (repoPagingItems.itemCount < 1) {
                if (repoPagingItems.loadState.refresh == LoadState.Loading) {
                    item {
                        Text(text = "加载中……")
                    }
                }
                else {
                    item {
                        EmptyItem()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserItem(
    userUiModel: UserUIModel,
    onClickItem: (user: UserUIModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        onClick = {
            onClickItem(userUiModel)
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            AvatarContent(
                data = userUiModel.avatarUrl ?: "",
                size = DpSize(35.dp, 35.dp),
                onClick = { onClickItem(userUiModel) }
            )
            Column {
                Text(
                    text = userUiModel.login ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = "person")
                    Text(text = userUiModel.name ?: "", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}