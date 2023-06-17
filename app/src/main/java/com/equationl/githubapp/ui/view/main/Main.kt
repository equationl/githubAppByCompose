package com.equationl.githubapp.ui.view.main

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Recommend
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.HomeTopBar
import com.equationl.githubapp.ui.view.dynamic.DynamicContent
import com.equationl.githubapp.ui.view.dynamic.DynamicViewAction
import com.equationl.githubapp.ui.view.dynamic.DynamicViewModel
import com.equationl.githubapp.ui.view.my.MyContent
import com.equationl.githubapp.ui.view.person.PersonAction
import com.equationl.githubapp.ui.view.person.PersonViewModel
import com.equationl.githubapp.ui.view.recommend.RecommendAction
import com.equationl.githubapp.ui.view.recommend.RecommendContent
import com.equationl.githubapp.ui.view.recommend.RecommendViewModel
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.fromJson
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onFinish: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    dynamicViewModel: DynamicViewModel = hiltViewModel(),
    recommendViewModel: RecommendViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = mainViewModel.viewStates
    val pagerState = rememberPagerState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val userInfo: User? = remember { DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson() }
    var updateContent by remember { mutableStateOf("") }
    val updateDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    LaunchedEffect(Unit) {
        mainViewModel.viewEvents.collect {
            when (it) {
                is MainViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
                is BaseEvent.ShowMsg -> {
                    launch {
                        drawerState.close()
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
                is MainViewEvent.HadUpdate -> {
                    updateContent = it.Content
                    updateDialogState.show()
                }
            }
        }
    }

    // 监听 pager 变化
    LaunchedEffect(pagerState) {
        withContext(Dispatchers.IO) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                mainViewModel.dispatch(MainViewAction.ScrollTo(MainPager.values()[page]))
            }
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.dispatch(MainViewAction.CheckUpdate(
            showTip = false,
            forceRequest = false,
            context = context
        ))
    }

    var lastClickTime = remember { 0L }
    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
        }
        else {
            if (System.currentTimeMillis() - lastClickTime > 2000) {
                lastClickTime = System.currentTimeMillis()
                mainViewModel.dispatch(BaseAction.ShowMag("再按一次退出"))
            }
            else {
                onFinish()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                user = userInfo ?: User(),
                viewModel = mainViewModel,
                navController = navController
            )
        },
        gesturesEnabled = viewState.gesturesEnabled
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                    HomeTopBar(
                        title = viewState.title,
                        navigationIcon = Icons.Outlined.Menu,
                        actions = {
                            IconButton(onClick = { navController.navigate(Route.SEARCH) }) {
                                Icon(Icons.Outlined.Search, "搜索")
                            }
                        },
                        onBack = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }
                    )
                }
            },
            bottomBar = {
                BottomBar(
                    viewState,
                    onScrollTo = {
                        coroutineScope.launch {
                            if (it == viewState.currentPage) { // 点击的是当前页面的按钮，回到顶部或刷新
                                when (it) {
                                    MainPager.HOME_DYNAMIC -> {
                                        dynamicViewModel.dispatch(DynamicViewAction.TopOrRefresh)
                                    }
                                    MainPager.HOME_RECOMMEND -> {
                                        recommendViewModel.dispatch(RecommendAction.TopOrRefresh)
                                    }
                                    MainPager.HOME_MY -> {
                                        personViewModel.dispatch(PersonAction.TopOrRefresh)
                                    }
                                }
                            }
                            else { // 点击的不是当前页面的按钮，跳转到点击的页面
                                pagerState.animateScrollToPage(it.ordinal)
                            }
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }}
        ) {
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(it)
            ) {
                MainContent(
                    pagerState,
                    drawerState,
                    navController,
                    scaffoldState,
                    viewState.gesturesEnabled,
                    dynamicViewModel,
                    recommendViewModel
                ) { enable ->
                    mainViewModel.dispatch(MainViewAction.ChangeGesturesEnabled(enable))
                }
            }

            UpdateDialog(context = context, dialogState = updateDialogState, content = updateContent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    pagerState: PagerState,
    drawerState: DrawerState,
    navController: NavHostController,
    scaffoldState: BottomSheetScaffoldState,
    gesturesEnabled: Boolean,
    dynamicViewModel: DynamicViewModel = hiltViewModel(),
    recommendViewModel: RecommendViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    onChangeGesturesEnabled: (enable: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        pageCount = 3,
        state = pagerState,
        beyondBoundsPageCount = 2,
        userScrollEnabled = gesturesEnabled && pagerState.currentPage != 0,
        modifier = Modifier.draggable(state = rememberDraggableState {
            if (drawerState.isClosed && !drawerState.isAnimationRunning) {
                if (it >= 5f) {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }
                else if (it < -5f && pagerState.canScrollForward && !pagerState.isScrollInProgress){
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            }
        },
        orientation = Orientation.Horizontal,
        enabled = pagerState.currentPage == 0)
    ) { page ->
        when (page) {
            0 -> DynamicContent(scaffoldState, navController, dynamicViewModel)
            1 -> RecommendContent(scaffoldState, navController, recommendViewModel)
            2 -> MyContent(scaffoldState, navController, personViewModel) {
                onChangeGesturesEnabled(it)
            }
        }
    }
}

@Composable
private fun MainDrawerContent(
    user: User,
    viewModel: MainViewModel,
    navController: NavHostController
) {
    val editDialogState: MaterialDialogState = rememberMaterialDialogState(false)
    val logoutDialogState: MaterialDialogState = rememberMaterialDialogState(false)
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 90.dp)
                .padding(vertical = 32.dp, horizontal = 8.dp)
        ) {
            AvatarContent(
                data = user.avatarUrl ?: "",
                size = DpSize(52.dp, 52.dp),
                onClick = {}
            )
            Text(
                text = user.login ?: "",
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Column(modifier = Modifier.width(232.dp)) {
            TextButton(onClick = { editDialogState.show() }) {
                Text(text = "用户反馈", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            TextButton(onClick = { navController.navigate(Route.USER_INFO) }) {
                Text(text = "个人信息", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            TextButton(onClick = {
                viewModel.dispatch(MainViewAction.CheckUpdate(
                    showTip = true,
                    forceRequest = true,
                    context = context
                ))
            }) {
                Text(text = "版本更新", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            TextButton(onClick = {
                context.browse("https://github.com/equationl/githubAppByCompose")
            }) {
                Text(text = "应用关于", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            TextButton(onClick = {
                viewModel.dispatch(MainViewAction.ClearCache(context))
            }) {
                Text(text = "清除缓存", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            TextButton(onClick = {
                logoutDialogState.show()
            }) {
                Text(text = "退出登陆", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, color = Color.Red)
            }
        }
    }

    FeedBackDialog(dialogState = editDialogState) {
        viewModel.dispatch(MainViewAction.PostFeedBack(it))
    }

    LogoutDialog(dialogState = logoutDialogState) {
        viewModel.dispatch(MainViewAction.Logout)

        navController.navigate(Route.LOGIN) {
            popUpTo(0)
        }
    }
}

@Composable
private fun UpdateDialog(
    context: Context,
    dialogState: MaterialDialogState,
    content: String
) {
    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {

            Text(text = content, modifier = Modifier.padding(vertical = 6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "取消")
                }

                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))

                TextButton(onClick = {
                    dialogState.hide()

                    context.browse("https://github.com/equationl/githubAppByCompose/releases")
                }) {
                    Text(text = "更新")
                }
            }
        }
    }
}

@Composable
private fun LogoutDialog(
    dialogState: MaterialDialogState,
    onConfirm: () -> Unit
) {
    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {

            Text(text = "确定要退出吗？", modifier = Modifier.padding(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "取消")
                }

                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))

                TextButton(onClick = {
                    dialogState.hide()

                    onConfirm()
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}

@Composable
private fun FeedBackDialog(
    dialogState: MaterialDialogState,
    onPostDate: (content: String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(text = "内容（支持 markdown）")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "取消")
                }

                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))

                TextButton(onClick = {
                    dialogState.hide()

                    onPostDate(content)
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    viewState: MainViewState,
    onScrollTo: (to: MainPager) -> Unit
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        BottomAppBar {
            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_DYNAMIC,
                title = "动态",
                iconUnselect = Icons.Outlined.DynamicFeed,
                iconSelect = Icons.Filled.DynamicFeed,
                onClick = { onScrollTo(MainPager.HOME_DYNAMIC) }
            )

            Spacer(Modifier.weight(1f, true))

            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_RECOMMEND,
                title = "推荐",
                iconUnselect = Icons.Outlined.Recommend,
                iconSelect = Icons.Filled.Recommend,
                onClick = { onScrollTo(MainPager.HOME_RECOMMEND) }
            )

            Spacer(Modifier.weight(1f, true))

            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_MY,
                title = "我的",
                iconUnselect = Icons.Outlined.Person,
                iconSelect = Icons.Filled.Person,
                onClick = { onScrollTo(MainPager.HOME_MY) }
            )

        }
    }
}

@Composable
fun RowScope.BottomItem(
    isSelected: Boolean,
    title: String,
    iconUnselect: ImageVector,
    iconSelect: ImageVector,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .weight(1f)) {
        Icon(
            if (isSelected) iconSelect else iconUnselect,
            title,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
        Text(title, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified)
    }
}
