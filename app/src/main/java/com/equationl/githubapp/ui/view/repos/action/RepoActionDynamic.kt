package com.equationl.githubapp.ui.view.repos.action

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.view.dynamic.EventRefreshContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoActionDynamicContent(
    userName: String,
    reposName: String,
    headerItem: LazyListScope.() -> Unit,
    scaffoldState: BottomSheetScaffoldState,
    navHostController: NavHostController,
    viewModel: RepoActionDynamicViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoActionDynamicAction.SetData(userName, reposName))
    }

    val dynamicList = viewState.dynamicFlow?.collectAsLazyPagingItems()

    if (dynamicList?.itemCount == 0 && viewModel.isInit && viewState.cacheDynamic.isNullOrEmpty()) {
        return
    }

    EventRefreshContent(
        navHostController = navHostController,
        eventPagingItems = dynamicList,
        cacheList = viewState.cacheDynamic,
        onLoadError = {
            viewModel.dispatch(RepoActionDynamicAction.ShowMsg(it))
        },
        onClickItem = {  },
        headerItem = headerItem
    )
}