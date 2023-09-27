package com.equationl.githubapp.ui.view.repos.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.equationl.githubapp.R
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.route.RouteParams
import com.equationl.githubapp.common.utlis.getImageLoader
import com.equationl.githubapp.model.bean.RepoPermission
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.LinkText
import com.equationl.githubapp.ui.common.VerticalIconText
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.repos.ReposPager
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposActionContent(
    userName: String,
    reposName: String,
    branch: String?,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    onChangePager: (pager: ReposPager) -> Unit,
    onGetDefaultBranch: (branch: String) -> Unit,
    onGetRepoPermission: (repoPermission: RepoPermission) -> Unit,
    viewModel: RepoActionViewModel = hiltViewModel()
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
        viewModel.dispatch(RepoActionAction.GetRepoInfo(userName, reposName))
    }

    LaunchedEffect(viewState.reposUIModel.defaultBranch) {
        if (!viewState.reposUIModel.defaultBranch.isNullOrBlank()) {
            onGetDefaultBranch(viewState.reposUIModel.defaultBranch!!)
        }
    }

    LaunchedEffect(viewState.reposUIModel.permission) {
        if (viewState.reposUIModel.defaultBranch != null) {
            onGetRepoPermission(viewState.reposUIModel.permission!!)
        }
    }


    when (viewState.currentTab) {
        RepoActionTab.Dynamic -> {
            // TODO 动态支持点击跳转
            RepoActionDynamicContent(
                userName = userName,
                reposName = reposName,
                repoPermission = viewState.reposUIModel.permission,
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
                branch = branch,
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
    Card(modifier = Modifier.fillMaxWidth()) {

        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(reposUIModel.ownerPic)
                .placeholder(R.drawable.empty_img)
                .build(),
            imageLoader = LocalContext.current.getImageLoader(),
            contentScale = ContentScale.Crop
        )

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .paint(
                    sizeToIntrinsics = false,
                    painter = painter,
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
                .padding(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinkText(
                    text = reposUIModel.ownerName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                ) {
                    navController.navigate("${Route.PERSON_DETAIL}/${reposUIModel.ownerName}")
                }
                Text(text = "/", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = reposUIModel.repositoryName, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Unspecified)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(text = reposUIModel.repositoryType)
                Text(text = reposUIModel.repositorySize, modifier = Modifier.padding(start = 6.dp))
                Text(text = reposUIModel.repositoryLicense, modifier = Modifier.padding(start = 6.dp))
            }

            Material3RichText(modifier = Modifier.padding(top = 4.dp)) {
                Markdown(content = reposUIModel.repositoryDes)
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = reposUIModel.repositoryAction)
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "最后提交于 ${reposUIModel.repositoryLastUpdateTime}")
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .horizontalScroll(rememberScrollState())
            ) {
                reposUIModel.repositoryTopics.forEach { topic ->
                    TopicItem(topic = topic) {
                        val searchQ = "topic:$topic"
                        navController.navigate("${Route.SEARCH}?${RouteParams.PAR_SEARCH_QUERY}=$searchQ")
                    }
                }
            }

            Divider(modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicItem(
    topic: String,
    onClickItem: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.padding(4.dp),
        onClick = onClickItem
    ) {
        Text(
            text = topic,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}