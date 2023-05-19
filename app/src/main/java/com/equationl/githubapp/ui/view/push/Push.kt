package com.equationl.githubapp.ui.view.push

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.FileUIModel
import com.equationl.githubapp.model.ui.PushUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefresh
import com.equationl.githubapp.ui.common.FileItem
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushDetailScreen(
    userName: String,
    repoName: String,
    sha: String,
    navController: NavHostController,
    viewModel: PushViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
                is PushEvent.Goto -> {
                    navController.navigate(it.routePath)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(PushAction.LoadData(userName, repoName, sha, backgroundColor, primaryColor))
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                title = repoName,
                actions = {
                    IconButton(onClick = { isShowDropMenu = !isShowDropMenu}) {
                        Icon(Icons.Outlined.MoreHoriz, "More")
                    }

                    MoreMenu(
                        isShow = isShowDropMenu,
                        onDismissRequest = { isShowDropMenu = false },
                        onClick =  {
                            viewModel.dispatch(PushAction.ClickMoreMenu(context, it, userName, repoName, sha))
                        }
                    )
                }
            ) {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            PushContent(
                isRefresh = viewState.isRefresh,
                pushUIModel = viewState.pushUIModel,
                itemList = viewState.fileUiModel,
                navController = navController,
                onClickFileItem = { fileUIModel ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val tempFile = File(context.externalCacheDir, "${fileUIModel.title}-${System.currentTimeMillis()}.temp")
                        tempFile.writeText(fileUIModel.patch)
                        val routePath = "${Route.CODE_DETAIL}/null/null/${fileUIModel.title}/${Uri.encode(tempFile.absolutePath)}/null"
                        viewModel.dispatch(PushAction.GoTo(routePath))
                    }
                },
                onRefresh = {
                    viewModel.dispatch(PushAction.LoadData(userName, repoName, sha, backgroundColor, primaryColor))
                }
            )
        }
    }
}

@Composable
private fun PushContent(
    isRefresh: Boolean,
    pushUIModel: PushUIModel,
    itemList: List<FileUIModel>,
    navController: NavHostController,
    onClickFileItem: (fileUiModel: FileUIModel) -> Unit,
    onRefresh: () -> Unit
) {

    BaseRefresh(
        isRefresh = isRefresh,
        itemList = itemList,
        itemUi = {
            FileItem(fileUiModel = it, onClickFileItem)
        },
        onRefresh = onRefresh,
        onClickItem = onClickFileItem,
        headerItem = {
            item(key = "Header") {
                Header(pushUIModel = pushUIModel, navController = navController)
            }
        }
    )
}

@Composable
private fun Header(
    pushUIModel: PushUIModel,
    navController: NavHostController
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarContent(
                data = pushUIModel.pushImage,
                size = DpSize(90.dp, 90.dp),
                userName = pushUIModel.pushUserName,
                navHostController = navController
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    Text(text = pushUIModel.pushEditCount)

                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    Text(text = pushUIModel.pushEditCount, modifier = Modifier.padding(start = 4.dp))

                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Remove")
                    Text(text = pushUIModel.pushEditCount, modifier = Modifier.padding(start = 4.dp))
                }

                Text(text = pushUIModel.pushTime)
                Text(text = pushUIModel.pushDes)
            }
        }
    }
}