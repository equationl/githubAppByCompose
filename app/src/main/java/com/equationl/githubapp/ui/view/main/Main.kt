package com.equationl.githubapp.ui.view.main

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.equationl.githubapp.ui.view.my.MyContent
import com.equationl.githubapp.ui.view.recommend.RecommendContent
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
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = viewModel.viewStates
    val pagerState = rememberPagerState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val userInfo: User? = remember { DataStoreUtils.getSyncData(DataKey.UserInfo, "").fromJson() }
    var updateContent by remember { mutableStateOf("") }
    val updateDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is MainViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
                is BaseEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
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
                viewModel.dispatch(MainViewAction.ScrollTo(MainPager.values()[page]))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(MainViewAction.CheckUpdate(false, context))
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
                viewModel.dispatch(BaseAction.ShowMag("再按一次退出"))
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
                viewModel = viewModel,
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
                            pagerState.animateScrollToPage(it.ordinal)
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
                MainContent(pagerState, navController, scaffoldState, viewState.gesturesEnabled) {enable ->
                    viewModel.dispatch(MainViewAction.ChangeGesturesEnabled(enable))
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
    navController: NavHostController,
    scaffoldState: BottomSheetScaffoldState,
    gesturesEnabled: Boolean,
    onChangeGesturesEnabled: (enable: Boolean) -> Unit
) {
    HorizontalPager(
        pageCount = 3,
        state = pagerState,
        userScrollEnabled = gesturesEnabled
    ) { page ->
        when (page) {
            0 -> DynamicContent(scaffoldState, navController)
            1 -> RecommendContent(scaffoldState, navController)
            2 -> MyContent(scaffoldState, navController) {
                onChangeGesturesEnabled(it)
            }
        }
    }
}

@Composable
private fun MainDrawerContent(
    user: User,
    viewModel: MainViewModel,
    navController: NavHostController,
) {
    val editDialogState: MaterialDialogState = rememberMaterialDialogState(false)
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

        Column {
            TextButton(onClick = { editDialogState.show() }) {
                Text(text = "用户反馈")
            }
            TextButton(onClick = { navController.navigate(Route.USER_INFO) }) {
                Text(text = "个人信息")
            }
            TextButton(onClick = {
                viewModel.dispatch(MainViewAction.CheckUpdate(true, context))
            }) {
                Text(text = "版本更新")
            }
            TextButton(onClick = {
                context.browse("https://github.com/equationl/githubAppByCompose")
            }) {
                Text(text = "应用关于")
            }
            TextButton(onClick = {
                viewModel.dispatch(MainViewAction.Logout)

                navController.popBackStack(Route.LOGIN, true)
            }) {
                Text(text = "退出登陆", color = Color.Red)
            }
        }
    }

    FeedBackDialog(dialogState = editDialogState) {
        viewModel.dispatch(MainViewAction.PostFeedBack(it))
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
                onScrollTo = { onScrollTo(MainPager.HOME_DYNAMIC) }
            )

            Spacer(Modifier.weight(1f, true))

            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_RECOMMEND,
                title = "推荐",
                iconUnselect = Icons.Outlined.Recommend,
                iconSelect = Icons.Filled.Recommend,
                onScrollTo = { onScrollTo(MainPager.HOME_RECOMMEND) }
            )

            Spacer(Modifier.weight(1f, true))

            BottomItem(
                isSelected = viewState.currentPage == MainPager.HOME_MY,
                title = "我的",
                iconUnselect = Icons.Outlined.Person,
                iconSelect = Icons.Filled.Person,
                onScrollTo = { onScrollTo(MainPager.HOME_MY) }
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
    onScrollTo: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onScrollTo)
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