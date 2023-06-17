package com.equationl.githubapp.ui.view.person

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
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
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.CustomWebView
import com.equationl.githubapp.ui.common.LinkText
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.view.dynamic.DynamicViewAction
import com.equationl.githubapp.ui.view.dynamic.DynamicViewEvent
import com.equationl.githubapp.ui.view.dynamic.EventRefreshContent
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.list.generalUser.GeneralUserListAction
import com.equationl.githubapp.ui.view.list.generalUser.GeneralUserListViewModel
import com.equationl.githubapp.ui.view.list.generalUser.UserListContent
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
        },
        floatingActionButton = {
            val focusState = viewModel.personViewState.isFollow
            if (focusState != IsFollow.NotNeed) {
                FloatingActionButton(onClick = { viewModel.dispatch(PersonAction.ChangeFollowState) }) {
                    Text(text = if (focusState == IsFollow.Followed) "取关" else "关注")
                }
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
    val userInfo: User? = remember { DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson() }

    LaunchedEffect(Unit) {
        if (userName != null) {
            viewModel.dispatch(PersonAction.GetUser(userName))
            viewModel.dispatch(DynamicViewAction.SetData(userName))
        }
        else {
            viewModel.dispatch(DynamicViewAction.SetData((userInfo?.login) ?: ""))
        }

        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
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

    when (viewModel.personViewState.user.type) {
        null -> { // 数据还没初始化
            PersonHeader(
                user = User(),
                navController = navController,
                isLoginUser = false,
                withChartMap = false
            )
        }
        "Organization" -> {  // 当前是一个组织，显示组织成员
            OrgMember(
                viewModel.personViewState.user,
                scaffoldState,
                navController
            )
        }
        else -> { // 当前是一个个人用户，显示用户动态
            PersonDynamic(
                navController = navController,
                viewModel = viewModel,
                isLoginUser = (userName == null || userInfo?.login == userName),
                onEnablePagerScroll = onEnablePagerScroll
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrgMember(
    user: User,
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController
) {
    val userListViewModel: GeneralUserListViewModel = hiltViewModel()
    val userListState = userListViewModel.viewStates

    LaunchedEffect(Unit) {
        userListViewModel.dispatch(GeneralUserListAction.SetData(user.login ?: "", "", GeneralListEnum.OrgMembers))

        userListViewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    val userList = userListState.userListFlow?.collectAsLazyPagingItems()

    UserListContent(
        userPagingItems = userList,
        cacheUserList = userListState.cacheUserList,
        isInit = userListViewModel.isInit,
        onLoadError = { msg ->
            userListViewModel.dispatch(BaseAction.ShowMag(msg))
        },
        onClickItem = { userUiModel ->
            navController.navigate("${Route.PERSON_DETAIL}/${userUiModel.login}")
        },
        headerItem = {
            item {
                PersonHeader(
                    user = user,
                    navController = navController,
                    isLoginUser = false,
                    withChartMap = false
                )
            }
        }
    )
}

@Composable
private fun PersonDynamic(
    navController: NavHostController,
    viewModel: PersonViewModel,
    isLoginUser: Boolean,
    onEnablePagerScroll: ((enable: Boolean) -> Unit)?
) {
    val personViewState = viewModel.personViewState
    val viewState = viewModel.viewStates
    val lazyListState: LazyListState = rememberLazyListState()


    val dynamicList = viewState.dynamicFlow?.collectAsLazyPagingItems()

    LaunchedEffect(dynamicList) {
        viewModel.viewEvents.collect {
            when (it) {
                is PersonEvent.TopOrRefresh -> {
                    if (lazyListState.firstVisibleItemIndex == 0) {
                        // refresh
                        dynamicList?.refresh()
                    }
                    else {
                        // scroll to top
                        lazyListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    if (dynamicList?.itemCount == 0 && viewModel.isInit && viewState.cacheList.isNullOrEmpty()) {
        return
    }

    var isScrollEnable by remember { mutableStateOf(true) }

    EventRefreshContent(
        navHostController = navController,
        eventPagingItems = dynamicList,
        cacheList = viewState.cacheList,
        isScrollEnable = isScrollEnable,
        lazyListState = lazyListState,
        onLoadError = {
            viewModel.dispatch(DynamicViewAction.ShowMsg(it))
        },
        onClickItem = {
            viewModel.dispatch(DynamicViewAction.ClickItem(it))
        },
        headerItem = {
            item(key = "header") {
                PersonHeader(
                    user = personViewState.user,
                    navController = navController,
                    isLoginUser = isLoginUser,
                    onEnablePagerScroll = {
                        isScrollEnable = it
                        onEnablePagerScroll?.invoke(it)
                    },
                )
            }
        },
        onRefresh = {
            viewModel.dispatch(PersonAction.GetUser(personViewState.user.login ?: ""))
        }
    )
}

@Composable
fun PersonHeader(
    user: User,
    navController: NavHostController,
    isLoginUser: Boolean,
    withChartMap: Boolean = true,
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
                    onClick = {
                           navController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(user.avatarUrl)}")
                    }
                )

                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.login ?: "加载中",
                            fontSize = 23.sp
                        )

                        if (isLoginUser) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .clickable {
                                        navController.navigate(Route.NOTIFY)
                                    }
                            )
                        }
                    }

                    user.name?.let {
                        Text(text = it)
                    }

                    IconText(imageVector = Icons.Filled.CorporateFare, text = user.company ?: "")
                    IconText(imageVector = Icons.Filled.Place, text = user.location ?: "")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.blog?.isNotEmpty() == true) {
                    Icon(imageVector = Icons.Filled.Link, contentDescription = null)
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
                VerticalText(topText = "仓库", bottomText = user.publicRepos.toString(), modifier = Modifier.clickable {
                    navController.navigate("${Route.REPO_LIST}/null/${user.login}/${GeneralListEnum.UserRepository.name}")
                })
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "粉丝", bottomText = user.followers.toString(), modifier = Modifier.clickable {
                    navController.navigate("${Route.USER_LIST}/null/${user.login}/${GeneralListEnum.UserFollower.name}")
                })
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "关注", bottomText = user.following.toString(), modifier = Modifier.clickable {
                    navController.navigate("${Route.USER_LIST}/null/${user.login}/${GeneralListEnum.UserFollowed.name}")
                })
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "星标", bottomText = user.starRepos.toString(), modifier = Modifier.clickable {
                    navController.navigate("${Route.REPO_LIST}/null/${user.login}/${GeneralListEnum.UserStar.name}")
                })
                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))
                VerticalText(topText = "荣耀", bottomText = user.honorRepos.toString(), modifier = Modifier.clickable {
                    navController.navigate("${Route.REPO_LIST}/null/${user.login}/${GeneralListEnum.UserHonor.name}")
                })
            }
        }

        if (withChartMap) {
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
}

@Composable
private fun IconText(imageVector: ImageVector, text: String, hideWhenTextBlank: Boolean = true) {
    if (!hideWhenTextBlank || text.isNotEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = imageVector, contentDescription = null)
            Text(text = text)
        }
    }
}

@Composable
private fun VerticalText(topText: String, bottomText: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = topText)
        Text(text = bottomText)
    }
}