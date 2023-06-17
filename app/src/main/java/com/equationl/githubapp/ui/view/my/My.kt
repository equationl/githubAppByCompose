package com.equationl.githubapp.ui.view.my

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.ui.view.person.PersonContent
import com.equationl.githubapp.ui.view.person.PersonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyContent(
    scaffoldState: BottomSheetScaffoldState,
    navController: NavHostController,
    viewModel: PersonViewModel = hiltViewModel(),
    onEnablePagerScroll: (enable: Boolean) -> Unit
) {
    PersonContent(
        scaffoldState = scaffoldState,
        navController = navController,
        userName = null, // 设置为 null 时表示使用当前登录用户信息
        onEnablePagerScroll,
        viewModel
    )
}