package com.equationl.githubapp.ui.view.repos.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.githubapp.R
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.getImageLoader
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.VerticalIconText
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.repos.ReposPager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposActionContent(
    userName: String,
    reposName: String,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    onChangePager: (pager: ReposPager) -> Unit,
    viewModel: RepoActionViewModel = hiltViewModel()
) {
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
        viewModel.dispatch(RepoActionAction.GetRepoInfo(userName, reposName))
    }

    val viewState = viewModel.viewStates

    when (viewState.currentTab) {
        RepoActionTab.Dynamic -> {
            RepoActionDynamicContent(
                userName = userName,
                reposName = reposName,
                headerItem = {
                    item {
                        ReposActionHeader(
                            viewState.reposUIModel,
                            viewState.currentTab,
                            navController,
                            onChangePager
                        ) {
                            viewModel.dispatch(RepoActionAction.ChangeTab(it))
                        }
                    }
                },
                scaffoldState = scaffoldState,
                navHostController = navController
            )
        }
        RepoActionTab.Commit -> {
            RepoActionCommitContent(
                userName = userName,
                reposName = reposName,
                headerItem = {
                    item {
                        ReposActionHeader(
                            viewState.reposUIModel,
                            viewState.currentTab,
                            navController,
                            onChangePager
                        ) {
                            viewModel.dispatch(RepoActionAction.ChangeTab(it))
                        }
                    }
                },
                scaffoldState = scaffoldState,
                navController = navController
            )
        }
    }
}

@Composable
private fun ReposActionHeader(
    reposUIModel: ReposUIModel,
    currentTab: RepoActionTab,
    navController: NavHostController,
    onChangePager: (pager: ReposPager) -> Unit,
    onChangeTab: (changeTo: RepoActionTab) -> Unit,
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(reposUIModel.ownerPic)
                    .placeholder(R.drawable.empty_img)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.2f,
                modifier = Modifier.fillMaxSize(),
                imageLoader = LocalContext.current.getImageLoader()
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = reposUIModel.ownerName, fontSize = 18.sp, color = Color.Unspecified)
                    Text(text = "/", fontSize = 18.sp)
                    Text(text = reposUIModel.repositoryName, fontSize = 18.sp, color = Color.Unspecified)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = reposUIModel.repositoryType)
                    Text(text = reposUIModel.repositorySize, modifier = Modifier.padding(start = 6.dp))
                    Text(text = reposUIModel.repositoryLicense, modifier = Modifier.padding(start = 6.dp))
                }
                
                Text(text = reposUIModel.repositoryDes)

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = reposUIModel.repositoryAction)
                }

                Divider(modifier = Modifier.padding(horizontal = 8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    VerticalIconText(icon = Icons.Outlined.StarBorder, text = reposUIModel.repositoryStar, modifier = Modifier.clickable {
                        navController.navigate("${Route.USER_LIST}/${reposUIModel.repositoryName}/${reposUIModel.ownerName}/${GeneralListEnum.RepositoryStarUser.name}")
                    })

                    Divider(modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp))

                    VerticalIconText(icon = Icons.Outlined.Share, text = reposUIModel.repositoryFork, modifier = Modifier.clickable {
                        navController.navigate("${Route.REPO_LIST}/${reposUIModel.repositoryName}/${reposUIModel.ownerName}/${GeneralListEnum.RepositoryForkUser.name}")
                    })

                    Divider(modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp))

                    VerticalIconText(icon = Icons.Outlined.Visibility, text = reposUIModel.repositoryWatch, modifier = Modifier.clickable {
                        navController.navigate("${Route.USER_LIST}/${reposUIModel.repositoryName}/${reposUIModel.ownerName}/${GeneralListEnum.RepositoryWatchUser.name}")
                    })

                    Divider(modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp))

                    VerticalIconText(icon = Icons.Outlined.Info, text = reposUIModel.repositoryIssue, modifier = Modifier.clickable {
                        onChangePager(ReposPager.Issue)
                    })

                    Divider(modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp))
                }
            }
        }
    }

    Card(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = {
                onChangeTab(RepoActionTab.Dynamic)
            }) {
                Text(
                    text = "动态",
                    color = if (currentTab == RepoActionTab.Dynamic) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            TextButton(onClick = {
                onChangeTab(RepoActionTab.Commit)
            }) {
                Text(
                    text = "提交",
                    color = if (currentTab == RepoActionTab.Commit) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}