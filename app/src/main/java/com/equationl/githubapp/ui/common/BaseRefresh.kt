package com.equationl.githubapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.equationl.githubapp.model.BaseUIModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


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
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    rememberSwipeRefreshState.isRefreshing = isRefresh

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
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
    var isInitiativeRefresh by remember { mutableStateOf(false) }

    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (pagingItems?.loadState?.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    rememberSwipeRefreshState.isRefreshing = (pagingItems?.loadState?.refresh is LoadState.Loading)

    if (!rememberSwipeRefreshState.isRefreshing) {
        isInitiativeRefresh = false
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = {
            onRefresh?.invoke()
            pagingItems?.refresh()
            isInitiativeRefresh = true
        },
        modifier = Modifier.fillMaxSize(),
        swipeEnabled = isScrollEnable
    ) {
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
                        itemUi(data = item, isRefresh)
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