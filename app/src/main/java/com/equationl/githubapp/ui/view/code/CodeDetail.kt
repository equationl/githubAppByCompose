package com.equationl.githubapp.ui.view.code

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.CustomWebView
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeDetailScreen(
    userName: String,
    reposName: String,
    path: String,
    localCode: String?,
    url: String?,
    navController: NavHostController,
    viewModel: CodeDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
        viewModel.dispatch(CodeDetailAction.LoadDate(context, userName, reposName, path, localCode))
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }
            TopBar(
                title = File(path).name,
                actions = {
                    val isEnable = (reposName != "null" && userName != "null" && path != "null")
                    IconButton(
                        enabled = isEnable,
                        onClick = {
                            isShowDropMenu = !isShowDropMenu
                        }
                    ) {
                        if (isEnable) {
                            Icon(Icons.Outlined.MoreHoriz, "More")
                        }

                        MoreMenu(
                            isShow = isShowDropMenu,
                            onDismissRequest = { isShowDropMenu = false },
                            onClick = {
                                viewModel.dispatch(CodeDetailAction.ClickMoreMenu(
                                    context = context,
                                    pos = it,
                                    userName = userName,
                                    reposName = reposName,
                                    url = url ?: ""
                                ))
                            }
                        )
                    }
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
        Column(modifier = Modifier.padding(it)) {
            CodeDetailContent(
                codeHtml = viewState.htmlContent,
                navController = navController
            )
        }
    }
}

@Composable
private fun CodeDetailContent(
    codeHtml: String,
    navController: NavHostController
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CustomWebView(
            url = "",
            htmlContent = codeHtml,
            onBack = {
                if (it?.canGoBack() == true) {
                    it.goBack()
                }
                else {
                    navController.popBackStack()
                }
            }
        )
    }
}

