package com.equationl.githubapp.ui.view.recommend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.common.constant.LanguageFilter
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefresh
import com.equationl.githubapp.ui.common.RepoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RecommendViewModel = hiltViewModel(),
) {
    val viewState = viewModel.viewStates

    val lazyListState: LazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is RecommendEvent.TopOrRefresh -> {
                    if (lazyListState.firstVisibleItemIndex == 0) {
                        // refresh
                        viewModel.dispatch(RecommendAction.RefreshData(true))
                    }
                    else {
                        // scroll to top
                        lazyListState.animateScrollToItem(0)
                    }
                }
                is RecommendEvent.ScrollToTop -> {
                    lazyListState.animateScrollToItem(0)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RecommendAction.RefreshData(false))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FilterHeader(
            sinceFilter = viewState.sinceFilter,
            languageFilter = viewState.languageFilter,
            onChangeSinceFilter = { viewModel.dispatch(RecommendAction.ChangeSinceFilter(it)) },
            onChangeLanguageFilter = { viewModel.dispatch(RecommendAction.ChangeLanguage(it)) }
        )

        RecommendRefreshContent(
            isRefreshing = viewState.isRefreshing,
            lazyListState = lazyListState,
            dataList = viewState.dataList,
            cacheList = viewState.cacheDataList,
            navController = navController
        ) {
            viewModel.dispatch(RecommendAction.RefreshData(true))
        }
    }
}

@Composable
private fun FilterHeader(
    sinceFilter: RecommendSinceFilter,
    languageFilter: LanguageFilter,
    onChangeSinceFilter: (choiceItem: RecommendSinceFilter) -> Unit,
    onChangeLanguageFilter: (choiceItem: LanguageFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        FilterDropMenu(title = sinceFilter.showName, options = RecommendSinceFilter.values(), onChoice = onChangeSinceFilter)
        Divider(modifier = Modifier
            .fillMaxHeight()
            .width(1.dp))
        FilterDropMenu(title = languageFilter.showName, options = LanguageFilter.values(), onChoice = onChangeLanguageFilter)
    }
}

@Composable
private fun <T>FilterDropMenu(
    title: String,
    options: Array<T>,
    onChoice: (choiceItem: T) -> Unit
) {
    var isShow by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.clickable {
                isShow = !isShow
            }
        ) {
            Text(text = title)
            Icon(
                imageVector = if (isShow) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        DropdownMenu(expanded = isShow, onDismissRequest = { isShow = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = {
                        when (item) {
                            is LanguageFilter -> {
                                Text(text = item.showName)
                            }
                            is RecommendSinceFilter -> {
                                Text(text = item.showName)
                            }
                        }
                    },
                    onClick = {
                        isShow = false
                        onChoice(item)
                    },
                )
            }
        }
    }
}

@Composable
private fun RecommendRefreshContent(
    isRefreshing: Boolean,
    lazyListState: LazyListState,
    dataList: List<ReposUIModel>,
    navController: NavHostController,
    cacheList: List<ReposUIModel>? = null,
    onRefresh: () -> Unit
) {

    BaseRefresh(
        isRefresh = isRefreshing,
        itemList = dataList,
        cacheItemList = cacheList,
        lazyListState = lazyListState,
        itemUi = {
            RepoItem(it, isRefreshing, navController) {
                navController.navigate("${Route.REPO_DETAIL}/${it.repositoryName}/${it.ownerName}")
            }
        },
        onRefresh = onRefresh,
        onClickItem = {},
        headerItem = {

        }
    )
}