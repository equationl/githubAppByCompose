package com.equationl.githubapp.ui.view.repos.readme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.githubapp.ui.common.CustomWebView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposReadmeContent(
    userName: String,
    reposName: String,
    scaffoldState: BottomSheetScaffoldState,
    viewModel: RepoReadmeViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is RepoReadMeEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(RepoReadMeAction.GetReadmeContent(reposName, userName))
    }


    Column(modifier = Modifier.fillMaxSize()) {
        CustomWebView(
            url = "",
            htmlContent = viewModel.viewStates.readmeContent,
            onBack = {}
        )
    }

}