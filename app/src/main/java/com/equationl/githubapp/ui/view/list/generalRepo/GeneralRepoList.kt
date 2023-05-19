package com.equationl.githubapp.ui.view.list.generalRepo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.common.constant.LocalCache
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.RepoItem
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.list.GeneralRepoListSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralRepoListScreen(
    repoName: String,
    userName: String,
    requestType: GeneralListEnum,
    navHostController: NavHostController,
    viewModel: GeneralRepoListViewModel = hiltViewModel()
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
        viewModel.dispatch(GeneralRepoListAction.SetData(userName, repoName, requestType))
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }
            val title = remember {
                when (requestType) {
                    GeneralListEnum.UserRepository -> "仓库"
                    GeneralListEnum.UserStar -> "星标"
                    GeneralListEnum.RepositoryForkUser -> "Fork"
                    GeneralListEnum.UserHonor -> "荣耀"
                    else -> "Null"
                }
            }
            TopBar(
                title = title,
                actions = {
                    val isEnable = remember {
                        requestType == GeneralListEnum.UserRepository || requestType == GeneralListEnum.UserStar
                    }
                    IconButton(
                        enabled = isEnable,
                        onClick = {
                            isShowDropMenu = !isShowDropMenu
                        }
                    ) {
                        if (isEnable) {
                            Icon(Icons.Outlined.Sort, "Sort")
                        }

                        SortMenu(
                            isShow = isShowDropMenu,
                            onDismissRequest = { isShowDropMenu = false },
                            requestType,
                            onClick = {
                                viewModel.dispatch(GeneralRepoListAction.SetData(userName, repoName, requestType, it))
                            }
                        )
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
        val repoList = viewState.repoListFlow?.collectAsLazyPagingItems()

        Column(modifier = Modifier.padding(it)) {
            GeneralRepoListContent(
                navHostController = navHostController,
                repoPagingItems = repoList,
                onLoadError = { msg ->
                    viewModel.dispatch(BaseAction.ShowMag(msg))
                },
                onClickItem = { repoUiModel ->
                    navHostController.navigate("${Route.REPO_DETAIL}/${repoUiModel.repositoryName}/${repoUiModel.ownerName}")
                },
                onRefresh = {
                    // 清除缓存
                    LocalCache.UserHonorCacheList = null
                }
            )
        }
    }
}

@Composable
fun GeneralRepoListContent(
    navHostController: NavHostController,
    repoPagingItems: LazyPagingItems<ReposUIModel>?,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (eventUiModel: ReposUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    BaseRefreshPaging(
        pagingItems = repoPagingItems,
        itemUi = {
            RepoItem(it, navHostController) {
                onClickItem(it)
            }
        },
        onLoadError = onLoadError,
        onClickItem = {},
        headerItem = headerItem,
        onRefresh = onRefresh
    )
}

@Composable
private fun SortMenu(
    isShow: Boolean,
    onDismissRequest: () -> Unit,
    requestType: GeneralListEnum,
    onClick: (item: GeneralRepoListSort) -> Unit
) {
    val options = remember {
        mutableListOf<GeneralRepoListSort>().apply {
            when (requestType) {
                GeneralListEnum.UserRepository -> {
                    add(GeneralRepoListSort.Push)
                    add(GeneralRepoListSort.Create)
                    add(GeneralRepoListSort.Name)
                }
                GeneralListEnum.UserStar -> {
                    add(GeneralRepoListSort.Stars)
                    add(GeneralRepoListSort.RecentlyStar)
                    add(GeneralRepoListSort.Update)
                }
                else -> {}
            }
        }
    }

    DropdownMenu(expanded = isShow, onDismissRequest = onDismissRequest) {
        Text(text = "排序方式：", Modifier.padding(start = 2.dp))
        options.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(text = item.showName)
                },
                onClick = {
                    onDismissRequest()
                    onClick(item)
                },
            )
        }
    }
}