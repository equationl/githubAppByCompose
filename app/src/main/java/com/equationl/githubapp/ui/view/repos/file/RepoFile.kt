package com.equationl.githubapp.ui.view.repos.file

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.model.ui.FileUIModel
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefresh
import com.equationl.githubapp.ui.common.FileItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposFileContent(
    userName: String,
    repoName: String,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: RepoFileViewModel = hiltViewModel()
) {
    val viewState  = viewModel.viewStates

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is RepoFileEvent.Refresh -> {
                    viewModel.dispatch(RepoFileAction.LoadData(repoName, userName))
                }
                is RepoFileEvent.GoTo -> {
                    navController.navigate(it.path)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoFileAction.LoadData(repoName, userName))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            pathList = viewState.pathList,
            onClick = {
                viewModel.dispatch(RepoFileAction.OnClickPath(it))
            }
        )

        RefreshContent(
            isRefresh = viewState.isRefresh,
            fileList = viewState.fileList,
            onClickFileItem = {
                viewModel.dispatch(RepoFileAction.OnClickFile(it, userName, repoName))
            },
            onRefresh = {
                viewModel.dispatch(RepoFileAction.LoadData(repoName, userName))
            }
        )
    }

    BackHandler {
        if (viewState.pathList.size == 1) {
            navController.popBackStack()
        }
        else {
            viewModel.dispatch(RepoFileAction.OnClickPath(viewState.pathList.lastIndex - 1))
        }
    }
}

@Composable
private fun RefreshContent(
    isRefresh: Boolean,
    fileList: List<FileUIModel>,
    onClickFileItem: (fileUiModel: FileUIModel) -> Unit,
    onRefresh: () -> Unit
) {
    BaseRefresh(
        isRefresh = isRefresh,
        itemList = fileList,
        itemUi = {
            FileItem(fileUiModel = it, onClickFileItem)
        },
        onRefresh = onRefresh,
        onClickItem = {}
    )
}

@Composable
private fun Header(
    pathList: List<String>,
    onClick: (pos: Int) -> Unit
) {
    val state = rememberLazyListState()
    LazyRow(
        state = state,
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(items = pathList) {index: Int, item: String ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item, modifier = Modifier.clickable {
                    onClick(index)
                })
                Text(text = " > ")
            }
        }
    }

    LaunchedEffect(pathList.size) {
        state.animateScrollToItem(pathList.size)
    }
}