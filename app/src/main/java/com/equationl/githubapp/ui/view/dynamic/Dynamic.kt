package com.equationl.githubapp.ui.view.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.ui.EventUIAction
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.EmptyItem
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vanpra.composematerialdialogs.MaterialDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: DynamicViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is DynamicViewEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
                is DynamicViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val userInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()
        viewModel.dispatch(DynamicViewAction.SetData((userInfo?.login) ?: ""))
    }

    if (viewState.dynamicFlow == null) {
        Text(text = "Need Init")
    }
    else {
        val dynamicList = viewState.dynamicFlow.collectAsLazyPagingItems()

        EventRefreshContent(
            navHostController = navController,
            eventPagingItems = dynamicList,
            onLoadError = {
                viewModel.dispatch(DynamicViewAction.ShowMsg(it))
            },
            onClickItem = {
                viewModel.dispatch(DynamicViewAction.ClickItem(it))
            }
        )
    }

    if (viewState.showChoosePushDialog) {
        EventChoosePushDialog(desList = viewState.pushShaDesList, valueList = viewState.pushShaList, onClickItem = {
            viewModel.dispatch(DynamicViewAction.ClickItem(it))
        })
    }
}

@Composable
fun EventChoosePushDialog(
    desList: List<String>,
    valueList: List<String>,
    onClickItem: (eventUiModel: EventUIModel) -> Unit
) {
    MaterialDialog {
        LazyColumn {
            itemsIndexed(desList) {index, value ->
                TextButton(onClick = {
                    val eventUiModel = EventUIModel(actionType = EventUIAction.Push, pushSha = arrayListOf(valueList[index]))
                    onClickItem(eventUiModel)
                }) {
                    Text(text = value)
                }
            }
        }
    }
}

@Composable
fun EventRefreshContent(
    navHostController: NavHostController,
    eventPagingItems: LazyPagingItems<EventUIModel>,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: EventUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (eventPagingItems.loadState.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    rememberSwipeRefreshState.isRefreshing = (eventPagingItems.loadState.refresh is LoadState.Loading)

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = {
            eventPagingItems.refresh()
            onRefresh?.invoke()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        EventLazyColumn(
            navHostController,
            eventPagingItems,
            onClickItem,
            headerItem = headerItem
        )
    }

}

@Composable
fun EventLazyColumn(
    navHostController: NavHostController,
    eventPagingItems: LazyPagingItems<EventUIModel>,
    onClickItem: (eventUiModel: EventUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp)
    ) {
        headerItem?.let { it() }

        itemsIndexed(eventPagingItems, key = {_, item -> item.id}) {_, item ->
            if (item != null) {
                Column(modifier = Modifier.padding(8.dp)) {
                    EventItem(
                        item.image,
                        item.username,
                        item.time,
                        item.action,
                        item.des,
                        navHostController
                    ) {
                        onClickItem(item)
                    }
                }
            }
        }

        if (eventPagingItems.itemCount < 1) {
            if (eventPagingItems.loadState.refresh == LoadState.Loading) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(
    avatarUrl: String,
    userName: String,
    timeText: String,
    action: String,
    des: String,
    navHostController: NavHostController,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = avatarUrl,
                        size = DpSize(50.dp, 50.dp),
                        navHostController = navHostController,
                        userName = userName
                    )

                    Text(
                        text = userName,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(text = timeText)
            }

            Text(text = action)

            if (des.isNotBlank()) {
                Text(text = des)
            }
        }
    }
}