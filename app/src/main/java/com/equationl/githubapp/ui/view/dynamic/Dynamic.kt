package com.equationl.githubapp.ui.view.dynamic

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.EventChoosePushDialog
import com.equationl.githubapp.ui.common.comPlaceholder
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.markdown.MarkdownParseOptions
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: DynamicViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates

    val dynamicList = viewState.dynamicFlow?.collectAsLazyPagingItems()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(dynamicList) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is DynamicViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
                is DynamicViewEvent.TopOrRefresh -> {
                    if (lazyListState.firstVisibleItemIndex == 0) {
                        // refresh
                        dynamicList?.refresh()
                    }
                    else {
                        // scroll to top
                        lazyListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val userInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()
        viewModel.dispatch(DynamicViewAction.SetData((userInfo?.login) ?: ""))
    }

    if (dynamicList?.itemCount == 0 && viewModel.isInit && viewState.cacheList.isNullOrEmpty()) {
        return
    }

    EventRefreshContent(
        navHostController = navController,
        eventPagingItems = dynamicList,
        cacheList = viewState.cacheList,
        lazyListState = lazyListState,
        onLoadError = {
            viewModel.dispatch(DynamicViewAction.ShowMsg(it))
        },
        onClickItem = {
            viewModel.dispatch(DynamicViewAction.ClickItem(it))
        },
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
    cacheList: List<EventUIModel>? = null,
    isScrollEnable: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: EventUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    BaseRefreshPaging(
        pagingItems = eventPagingItems,
        cacheItems = cacheList,
        itemUi = {data, isRefresh ->
            Column(modifier = Modifier.padding(8.dp)) {
                EventItem(
                    data.image,
                    data.username,
                    data.time,
                    data.action,
                    data.des,
                    isRefresh,
                    navHostController
                ) {
                    onClickItem(data)
                }
            }
        },
        isScrollEnable = isScrollEnable,
        lazyListState = lazyListState,
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
    isRefresh: Boolean,
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
                        userName = userName,
                        isRefresh = isRefresh
                    )

                    Text(
                        text = userName,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .comPlaceholder(isRefresh),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(text = timeText, modifier = Modifier.comPlaceholder(isRefresh))
            }

            Material3RichText(modifier = Modifier.comPlaceholder(isRefresh)) {
                Markdown(
                    content = action + if (des.isNotBlank()) " : " else "",
                    markdownParseOptions = MarkdownParseOptions(autolink = false),
                    onImgClicked = {
                        navHostController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(it)}")
                    }
                )
            }

            if (des.isNotBlank()) {
                Material3RichText(modifier = Modifier
                    .padding(top = 8.dp)
                    .comPlaceholder(isRefresh)) {
                    Markdown(
                        content = des,
                        markdownParseOptions = MarkdownParseOptions.Default.copy(autolink = false),
                        onImgClicked = {
                            navHostController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(it)}")
                        }
                    )
                }
            }
        }
    }
}