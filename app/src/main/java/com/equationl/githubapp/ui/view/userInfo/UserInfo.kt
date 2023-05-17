package com.equationl.githubapp.ui.view.userInfo

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.TopBar
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    navHostController: NavHostController,
    viewModel: UserInfoViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val editDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(UserInfoAction.UpdateUserInfo)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "个人信息"
            ) {
                navHostController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        var type by remember { mutableStateOf(UserInfoItemType.Name) }
        var rawContent by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(it)) {
            UserInfoContent(user = viewState.userInfo) { itemType, content ->
                type = itemType
                rawContent = content
                Log.i("el", "UserInfoScreen: rawContent=$rawContent")
                editDialogState.show()
            }
        }

        EditDialog(dialogState = editDialogState, rawContent = rawContent, type = type) { itemType, content ->
            viewModel.dispatch(UserInfoAction.EditUserInfo(itemType, content))
        }
    }
}



@Composable
private fun UserInfoContent(
    user: User,
    onClick: (type: UserInfoItemType, rawContent: String) -> Unit
) {
    UserInfoLazyColumn(user = user, onClick = onClick)
}

@Composable
private fun UserInfoLazyColumn(
    user: User,
    onClick: (type: UserInfoItemType, rawContent: String) -> Unit
) {
    LazyColumn {
        items(UserInfoItemType.values()) {
            val rawContent = it.getItemValue(user) ?: ""
            InfoItem(icon = it.Icon, title = it.title, value = rawContent) {
                onClick(it, rawContent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .weight(9f)) {
                Icon(imageVector = icon, contentDescription = title, modifier = Modifier.padding(start = 6.dp))
                Text(text = title, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 6.dp))
                Text(text = value, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 6.dp))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(imageVector = Icons.Filled.ArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun EditDialog(
    dialogState: MaterialDialogState,
    rawContent: String,
    type: UserInfoItemType,
    onPostDate: (type: UserInfoItemType, content: String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    content = rawContent

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
                    Text(text = "内容")
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

                    onPostDate(type, content)
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}