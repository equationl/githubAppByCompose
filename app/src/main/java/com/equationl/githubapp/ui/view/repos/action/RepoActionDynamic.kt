package com.equationl.githubapp.ui.view.repos.action

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.ui.view.dynamic.DynamicRefreshContent

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
                is RepoActionDynamicEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoActionDynamicAction.SetData(userName, reposName))
    }

    if (viewState.dynamicFlow != null) {
        val dynamicList = viewState.dynamicFlow.collectAsLazyPagingItems()
        DynamicRefreshContent(
            navHostController = navHostController,
            eventPagingItems = dynamicList,
            onLoadError = {
                viewModel.dispatch(RepoActionDynamicAction.ShowMsg(it))
            },
            onClickItem = {  },
            headerItem = headerItem
        )
    }
    else {
        Text(text = "Need init...")
    }
}