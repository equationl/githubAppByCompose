package com.equationl.githubapp.ui.view.notify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.EventChoosePushDialog
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.dynamic.EventRefreshContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifyScreen(
    navHostController: NavHostController,
    viewModel: NotifyViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is NotifyEvent.Goto -> {
                    navHostController.navigate(it.route)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(NotifyAction.ApplyFilter(NotifyRequestFilter.UnRead))
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "通知",
                actions = {
                    IconButton(onClick = { viewModel.dispatch(NotifyAction.ReadAll) }) {
                        Icon(imageVector = Icons.Filled.DoneAll, contentDescription = "all read")
                    }
                }
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
        Column(modifier = Modifier.padding(it)) {
            NotifyContent(
                navHostController = navHostController,
                eventPagingItems = viewState.notifyFlow?.collectAsLazyPagingItems(),
                onLoadError = { msg ->
                    viewModel.dispatch(BaseAction.ShowMag(msg))
                },
                onClickItem = { uiModel ->
                    viewModel.dispatch(NotifyAction.ClickItem(uiModel))
                },
                headerItem = {
                    item(key = "Header") {
                        HeaderTab(currentTab = viewState.requestFilter) { filter ->
                            viewModel.dispatch(NotifyAction.ApplyFilter(filter))
                        }
                    }
                }
            )
        }

        if (viewState.showChoosePushDialog) {
            EventChoosePushDialog(desList = viewState.pushShaDesList, valueList = viewState.pushShaList, onClickItem = { uiModel ->
                viewModel.dispatch(NotifyAction.ClickItem(uiModel))
            })
        }
    }
}

@Composable
private fun NotifyContent(
    navHostController: NavHostController,
    eventPagingItems: LazyPagingItems<EventUIModel>?,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: EventUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    EventRefreshContent(
        navHostController = navHostController,
        eventPagingItems = eventPagingItems,
        cacheList = null,
        onLoadError = onLoadError,
        onClickItem = onClickItem,
        headerItem = headerItem,
        onRefresh = onRefresh
    )
}

@Composable
private fun HeaderTab(
    currentTab: NotifyRequestFilter,
    onChangeTab: (filter: NotifyRequestFilter) -> Unit
) {
    Card(modifier = Modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            NotifyRequestFilter.values().forEach {tab ->
                TextButton(onClick = {
                    onChangeTab(tab)
                }) {
                    Text(
                        text = tab.showText,
                        color = if (currentTab == tab) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}