package com.equationl.githubapp.ui.view.person

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.CustomWebView
import com.equationl.githubapp.ui.common.LinkText
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.dynamic.DynamicRefreshContent
import com.equationl.githubapp.ui.view.dynamic.DynamicViewAction
import com.equationl.githubapp.ui.view.dynamic.DynamicViewEvent
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    userName: String,
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState()

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                title = userName,
                actions = {
                    IconButton(onClick = { isShowDropMenu = !isShowDropMenu}) {
                        Icon(Icons.Outlined.MoreHoriz, "More")
                    }

                    MoreMenu(
                        isShow = isShowDropMenu,
                        onDismissRequest = { isShowDropMenu = false },
                        onClick =  {
                            viewModel.dispatch(PersonAction.ClickMoreMenu(context, it, userName))
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
            PersonContent(
                scaffoldState = scaffoldState,
                navController = navController,
                userName = userName
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    userName: String? = null,
    onEnablePagerScroll: ((enable: Boolean) -> Unit)? = null,
    viewModel: PersonViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val personViewState = viewModel.personViewState

    LaunchedEffect(Unit) {
        if (userName != null) {
            viewModel.dispatch(PersonAction.GetUser(userName))
            viewModel.dispatch(DynamicViewAction.SetData(userName))
        }
        else {
            val userInfo: User? = DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson()
            viewModel.dispatch(DynamicViewAction.SetData((userInfo?.login) ?: ""))
        }

        viewModel.viewEvents.collect {
            when (it) {
                is DynamicViewEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is DynamicViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
            }
        }
    }

    if (viewState.dynamicFlow == null) {
        Text(text = "Need init")
    }
    else {
        val dynamicList = viewState.dynamicFlow.collectAsLazyPagingItems()
        DynamicRefreshContent(
            navHostController = navController,
            eventPagingItems = dynamicList,
            onLoadError = {
                viewModel.dispatch(DynamicViewAction.ShowMsg(it))
            },
            onClickItem = {
                viewModel.dispatch(DynamicViewAction.ClickItem(it))
            },
            headerItem = {
                item(key = "header") {
                    PersonHeader(
                        personViewState.user,
                        navController,
                        onEnablePagerScroll
                    )
                }
            },
            onRefresh = {
                viewModel.dispatch(PersonAction.GetUser(personViewState.user.login ?: ""))
            }
        )
    }
}

@Composable
fun PersonHeader(
    user: User,
    navController: NavHostController,
    onEnablePagerScroll: ((enable: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current

    Column {
        Card(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarContent(
                    data = user.avatarUrl ?: "",
                    size = DpSize(50.dp, 50.dp),
                    userName = user.login ?: "",
                    navHostController = navController
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.login ?: "",
                            fontSize = 23.sp
                        )

                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.clickable { /*TODO*/ }
                        )
                    }

                    Text(text = user.name ?: "")

                    IconText(imageVector = Icons.Filled.CropOriginal, text = user.company ?: "")
                    IconText(imageVector = Icons.Filled.LocationCity, text = user.location ?: "")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Web, contentDescription = null)
                LinkText(text = user.blog ?: "") {
                    user.blog?.let {
                        var url = it
                        if (!url.startsWith("http://") || !url.startsWith("https://")) {
                            url = "http://$url"
                        }
                        val uri = Uri.parse(url)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                }
            }

            Text(text = user.bio ?: "")

            Text(text = CommonUtils.getDateStr(user.createdAt))


            Divider(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VerticalText(topText = "Repo", bottomText = user.publicRepos.toString() )
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "Followers", bottomText = user.followers.toString())
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "Following", bottomText = user.following.toString())
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "Star", bottomText = user.starRepos.toString())
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "Honer", bottomText = user.honorRepos.toString())
            }
        }

        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
        ) {
            CustomWebView(
                url = CommonUtils.getUserChartAddress(user.login ?: "", MaterialTheme.colorScheme.primary),
                onTouchEvent = {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onEnablePagerScroll?.invoke(false)
                            false
                        }
                        MotionEvent.ACTION_UP -> {
                            onEnablePagerScroll?.invoke(true)
                            false
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            onEnablePagerScroll?.invoke(true)
                            false
                        }
                        else -> {
                            false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun IconText(imageVector: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = imageVector, contentDescription = null)
        Text(text = text)
    }
}

@Composable
fun VerticalText(topText: String, bottomText: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = topText)
        Text(text = bottomText)
    }
}