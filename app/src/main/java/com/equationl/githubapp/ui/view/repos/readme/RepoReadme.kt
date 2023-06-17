package com.equationl.githubapp.ui.view.repos.readme

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.ui.theme.getRichTextStyle
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposReadmeContent(
    userName: String,
    reposName: String,
    branch: String?,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
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

    LaunchedEffect(branch) {
        viewModel.dispatch(RepoReadMeAction.GetReadmeContent(reposName, userName, branch))
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
        Material3RichText(
            style = getRichTextStyle(),
            modifier = Modifier.fillMaxSize()
        ) {
            Markdown(
                content = viewModel.viewStates.readmeContent,
                onImgClicked = {
                    navController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(it)}")
                }
            )
        }
    }

}