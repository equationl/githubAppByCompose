package com.equationl.githubapp.ui.view.recommend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.common.constant.LanguageFilter
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.VerticalIconText
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ireward.htmlcompose.HtmlText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RecommendViewModel = hiltViewModel(),
) {
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is RecommendEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RecommendAction.RefreshData)
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
            dataList = viewState.dataList,
            navController = navController
        ) {
            viewModel.dispatch(RecommendAction.RefreshData)
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
    dataList: List<ReposUIModel>,
    navController: NavHostController,
    onRefresh: () -> Unit
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    rememberSwipeRefreshState.isRefreshing = isRefreshing

    SwipeRefresh(
        state = rememberSwipeRefreshState,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        RecommendLazyColumn(dataList, navController)
    }
}

@Composable
private fun RecommendLazyColumn(
    dataList: List<ReposUIModel>,
    navController: NavHostController,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp)
    ) {
        items(
            items = dataList,
            key = {
                it.lazyColumnKey
            }
        ) {
            RepoItem(it, navController) {
                navController.navigate("${Route.REPO_DETAIL}/${it.repositoryName}/${it.ownerName}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoItem(
    data: ReposUIModel,
    navController: NavHostController,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = data.ownerPic,
                        navHostController = navController,
                        userName = data.ownerName
                    )

                    Column(
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = data.repositoryName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                            Text(text = data.ownerName)
                        }
                    }
                }

                Text(text = data.repositoryType)
            }

            HtmlText(
                text = data.repositoryDes
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                VerticalIconText(icon = Icons.Filled.StarBorder, text = data.repositoryStar)
                VerticalIconText(icon = Icons.Filled.Share, text = data.repositoryFork)
                VerticalIconText(icon = Icons.Filled.Visibility, text = data.repositoryWatch)
            }
        }
    }
}