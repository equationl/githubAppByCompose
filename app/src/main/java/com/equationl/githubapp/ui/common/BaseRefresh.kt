package com.equationl.githubapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.githubapp.model.BaseUIModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@Composable
fun <T: BaseUIModel>BaseRefresh(
    isRefresh: Boolean,
    itemList: List<T>,
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
            itemUi = itemUi,
            onClickFileItem = onClickItem,
            headerItem = headerItem,
        )
    }
}

@Composable
private fun <T: BaseUIModel>BaseRefreshLazyColumn(
    itemList: List<T>,
    itemUi: @Composable ColumnScope.(data: T) -> Unit,
    onClickFileItem: (fileUiModel: T) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        headerItem?.let {
            headerItem()
        }

        items(
            items = itemList,
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
    itemUi: @Composable ColumnScope.(data: T) -> Unit,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: T) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    emptyItem: @Composable () -> Unit = { EmptyItem() }
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (pagingItems?.loadState?.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    rememberSwipeRefreshState.isRefreshing = (pagingItems?.loadState?.refresh is LoadState.Loading)

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = {
            onRefresh?.invoke()
            pagingItems?.refresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        BasePagingLazyColumn(
            pagingItems,
            itemUi,
            onClickItem,
            emptyItem = emptyItem,
            headerItem = headerItem
        )
    }
}

@Composable
private fun <T: BaseUIModel>BasePagingLazyColumn(
    pagingItems: LazyPagingItems<T>?,
    itemUi: @Composable ColumnScope.(data: T) -> Unit,
    onClickItem: (eventUiModel: T) -> Unit,
    emptyItem: @Composable () -> Unit = { EmptyItem() },
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp)
    ) {
        headerItem?.let { it() }

        if (pagingItems == null) {
            item {
                EmptyItem(true)
            }
        }
        else {
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

            if (pagingItems.itemCount < 1) {
                if (pagingItems.loadState.refresh == LoadState.Loading) {
                    item {
                        LoadItem()
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