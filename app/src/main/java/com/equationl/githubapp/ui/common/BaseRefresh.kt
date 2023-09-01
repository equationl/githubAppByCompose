package com.equationl.githubapp.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.equationl.githubapp.R
import com.equationl.githubapp.model.BaseUIModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


/**
 * 下拉刷新动效与实际内容之间的间距
 * */
private const val SPACE = 20

@Composable
fun <T: BaseUIModel>BaseRefresh(
    isRefresh: Boolean,
    itemList: List<T>,
    cacheItemList: List<T>? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    itemUi: @Composable ColumnScope.(data: T) -> Unit,
    onRefresh: () -> Unit,
    onClickItem: (item: T) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    val refreshTriggerDistance = remember { 180.dp }

    @Suppress("DEPRECATION")
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    var contentYOffsetDuration by remember { mutableStateOf(0) }
    var contentYOffsetTarget by remember { mutableStateOf(0.dp) }
    val contentYOffset by animateDpAsState(
        targetValue = contentYOffsetTarget,
        label = "contentYOffset",
        animationSpec = tween(
            durationMillis = contentYOffsetDuration
        )
    )

    rememberSwipeRefreshState.isRefreshing = isRefresh

    @Suppress("DEPRECATION")
    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicator = { state, trigger ->
            AnimationIndicator(
                swipeRefreshState = state,
                refreshTriggerDistance = trigger
            )
        },
        refreshTriggerDistance = refreshTriggerDistance
    ) {
        if (rememberSwipeRefreshState.isRefreshing) {
            contentYOffsetTarget = refreshTriggerDistance + SPACE.dp
        }
        else {
            if (rememberSwipeRefreshState.isSwipeInProgress) {
                with(LocalDensity.current) {
                    contentYOffsetTarget = rememberSwipeRefreshState.indicatorOffset.toDp().coerceAtMost(refreshTriggerDistance + SPACE.dp)
                }
                contentYOffsetDuration = 0
            }
            else {
                contentYOffsetTarget = 0.dp
                contentYOffsetDuration = 300
            }
        }

        Column(modifier = Modifier.offset(y = contentYOffset)) {
            BaseRefreshLazyColumn(
                itemList = itemList,
                cacheItemList = cacheItemList,
                lazyListState = lazyListState,
                itemUi = itemUi,
                onClickFileItem = onClickItem,
                headerItem = headerItem,
            )
        }
    }
}

@Composable
private fun <T: BaseUIModel>BaseRefreshLazyColumn(
    itemList: List<T>,
    cacheItemList: List<T>? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    itemUi: @Composable ColumnScope.(data: T) -> Unit,
    onClickFileItem: (fileUiModel: T) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        headerItem?.let {
            headerItem()
        }

        val realItemList = cacheItemList ?: itemList

        items(
            items = realItemList,
            key = { item -> item.lazyColumnKey }
        ) {
            Column(modifier = Modifier.clickable { onClickFileItem(it) }) {
                itemUi(it)
            }
        }
    }
}


@Composable
fun <T : BaseUIModel>BaseRefreshPaging(
    pagingItems: LazyPagingItems<T>?,
    cacheItems: List<T>? = null,
    isScrollEnable: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    itemUi: @Composable ColumnScope.(data: T, isRefresh: Boolean) -> Unit,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: T) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    emptyItem: @Composable () -> Unit = { EmptyItem() }
) {
    val refreshTriggerDistance = remember { 180.dp }
    var isInitiativeRefresh by remember { mutableStateOf(false) }

    @Suppress("DEPRECATION")
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    var contentYOffsetDuration by remember { mutableStateOf(0) }
    var contentYOffsetTarget by remember { mutableStateOf(0.dp) }
    val contentYOffset by animateDpAsState(
        targetValue = contentYOffsetTarget,
        label = "contentYOffset",
        animationSpec = tween(
            durationMillis = contentYOffsetDuration
        )
    )

    if (pagingItems?.loadState?.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    rememberSwipeRefreshState.isRefreshing = (pagingItems?.loadState?.refresh is LoadState.Loading)

    if (!rememberSwipeRefreshState.isRefreshing) {
        isInitiativeRefresh = false
    }

    @Suppress("DEPRECATION")
    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = {
            onRefresh?.invoke()
            pagingItems?.refresh()
            isInitiativeRefresh = true
        },
        modifier = Modifier.fillMaxSize(),
        swipeEnabled = isScrollEnable,
        indicator = { state, trigger ->
            AnimationIndicator(
                swipeRefreshState = state,
                refreshTriggerDistance = trigger
            )
        },
        refreshTriggerDistance = refreshTriggerDistance
    ) {
        if (rememberSwipeRefreshState.isRefreshing) {
            contentYOffsetTarget = refreshTriggerDistance + SPACE.dp
        }
        else {
            if (rememberSwipeRefreshState.isSwipeInProgress) {
                with(LocalDensity.current) {
                    contentYOffsetTarget = rememberSwipeRefreshState.indicatorOffset.toDp().coerceAtMost(refreshTriggerDistance + SPACE.dp)
                }
                contentYOffsetDuration = 0
            }
            else {
                contentYOffsetTarget = 0.dp
                contentYOffsetDuration = 300
            }
        }

        Column(modifier = Modifier.offset(y = contentYOffset)) {
            BasePagingLazyColumn(
                pagingItems = pagingItems,
                cacheItems = cacheItems,
                itemUi = itemUi,
                isRefresh = isInitiativeRefresh,
                isScrollEnable = isScrollEnable,
                lazyListState = lazyListState,
                onClickItem = onClickItem,
                emptyItem = emptyItem,
                headerItem = headerItem
            )
        }
    }
}

@Composable
private fun <T: BaseUIModel>BasePagingLazyColumn(
    pagingItems: LazyPagingItems<T>?,
    cacheItems: List<T>? = null,
    isRefresh: Boolean,
    isScrollEnable: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    itemUi: @Composable ColumnScope.(data: T, isRefresh: Boolean) -> Unit,
    onClickItem: (eventUiModel: T) -> Unit,
    emptyItem: @Composable () -> Unit = { EmptyItem() },
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp),
        userScrollEnabled = isScrollEnable
    ) {
        headerItem?.let { it() }

        if (pagingItems == null) {
            item {
                EmptyItem(true)
            }
        }
        else {
            val count = cacheItems?.size ?: pagingItems.itemCount
            items(count, key = {
                if (cacheItems == null) pagingItems.peek(it)!!.lazyColumnKey else cacheItems[it].lazyColumnKey
            }) {
                val item = if (cacheItems == null) pagingItems[it] else cacheItems[it]
                if (item != null) {
                    Column(
                        modifier = Modifier.clickable {
                            onClickItem(item)
                        }
                    ) {
                        itemUi(item, isRefresh)
                    }
                }
            }

            /*if (cacheItems == null) {
                itemsIndexed(pagingItems, key = { _, item -> item.lazyColumnKey}) { _, item ->
                    if (item != null) {
                        Column(
                            modifier = Modifier.clickable {
                                onClickItem(item)
                            }
                        ) {
                            itemUi(data = item)
                        }
                    }
                }
            }
            else {
                itemsIndexed(items = cacheItems, key = {_, item -> item.lazyColumnKey}) {_, item ->
                    Column(
                        modifier = Modifier.clickable {
                            onClickItem(item)
                        }
                    ) {
                        itemUi(data = item)
                    }
                }
            }*/

            if (pagingItems.itemCount < 1) {
                if (pagingItems.loadState.refresh == LoadState.Loading) {
                    item {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            LoadItem()
                        }
                    }
                }
                else {
                    item {
                        emptyItem()
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun AnimationIndicator(
    swipeRefreshState: SwipeRefreshState,
    refreshTriggerDistance: Dp,
) {
    var tipText by remember { mutableStateOf("") }

    val trigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val totalProgress = (swipeRefreshState.indicatorOffset / (trigger + with(LocalDensity.current) { SPACE.dp.toPx() * 3 })).coerceIn(0f, 1f)


    var animationAlpha by remember { mutableStateOf(1f) }

    var isPlaying by remember { mutableStateOf(false) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val animationProgress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever, isPlaying = isPlaying)

    var animationTopOffset by remember { mutableStateOf(-refreshTriggerDistance) }

    if (swipeRefreshState.isRefreshing) {
        tipText = "正在加载中……"
        animationTopOffset = 0.dp
        isPlaying = true
        animationAlpha = 1f
    }
    else {
        animationTopOffset = with(LocalDensity.current) {
            (swipeRefreshState.indicatorOffset.toDp() - refreshTriggerDistance).coerceAtMost(0.dp)
        }

        isPlaying = false
        animationAlpha = totalProgress // FastOutSlowInEasing.transform(totalProgress)

        tipText = if (swipeRefreshState.indicatorOffset < trigger) {
            "继续下拉以刷新"
        } else {
            "松手立即刷新"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(refreshTriggerDistance)
            .offset(y = animationTopOffset),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LottieAnimation(
            composition = composition,
            progress = {
                if (swipeRefreshState.isRefreshing) {
                    animationProgress
                }
                else {
                    totalProgress
                }
            },
            modifier = Modifier
                .height(refreshTriggerDistance - SPACE.dp)
                .alpha(animationAlpha)
        )

        Text(text = tipText)
    }
}