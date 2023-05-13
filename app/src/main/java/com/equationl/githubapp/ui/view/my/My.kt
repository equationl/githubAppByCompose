package com.equationl.githubapp.ui.view.my

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.equationl.githubapp.ui.view.person.PersonContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    onEnablePagerScroll: (enable: Boolean) -> Unit
) {
    PersonContent(
        scaffoldState = scaffoldState,
        navController = navController,
        userName = null, // 设置为 null 时表示使用当前登录用户信息
        onEnablePagerScroll
    )
}