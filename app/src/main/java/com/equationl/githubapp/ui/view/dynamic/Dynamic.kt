package com.equationl.githubapp.ui.view.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.EventChoosePushDialog
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson

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

    val dynamicList = viewState.dynamicFlow?.collectAsLazyPagingItems()

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

    if (viewState.showChoosePushDialog) {
        EventChoosePushDialog(desList = viewState.pushShaDesList, valueList = viewState.pushShaList, onClickItem = {
            viewModel.dispatch(DynamicViewAction.ClickItem(it))
        })
    }
}

@Composable
fun EventRefreshContent(
    navHostController: NavHostController,
    eventPagingItems: LazyPagingItems<EventUIModel>?,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: EventUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    BaseRefreshPaging(
        pagingItems = eventPagingItems,
        itemUi = {
            Column(modifier = Modifier.padding(8.dp)) {
                EventItem(
                    it.image,
                    it.username,
                    it.time,
                    it.action,
                    it.des,
                    navHostController
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
private fun EventItem(
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