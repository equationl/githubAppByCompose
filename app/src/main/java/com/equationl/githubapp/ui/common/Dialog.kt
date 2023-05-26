package com.equationl.githubapp.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.equationl.githubapp.model.ui.EventUIAction
import com.equationl.githubapp.model.ui.EventUIModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

@Composable
fun EventChoosePushDialog(
    desList: List<String>,
    valueList: List<String>,
    onClickItem: (eventUiModel: EventUIModel) -> Unit
) {
    MaterialDialog {
        LazyColumn {
            itemsIndexed(desList) {index, value ->
                TextButton(onClick = {
                    val eventUiModel = EventUIModel(actionType = EventUIAction.Push, pushSha = arrayListOf(valueList[index]))
                    onClickItem(eventUiModel)
                }) {
                    Text(text = value)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(
    permissionState: PermissionState,
    dialogState: MaterialDialogState = rememberMaterialDialogState(),
    callBack: (isGranted: Boolean) -> Unit
) {
    if (dialogState.showing) {
        if (permissionState.status.isGranted) {
            callBack(true)
            dialogState.hide()
        }
        else {
            val text = if (permissionState.status.shouldShowRationale) "我们需要储存权限才能保存图片" else "请授予储存权限以保存图片"
            MaterialDialog(
                dialogState = dialogState,
                buttons = {
                    button(
                        text = "授权",
                        onClick = {
                            permissionState.launchPermissionRequest()
                        }
                    )
                    button(
                        text = "取消",
                        onClick = {
                            callBack(false)
                        }
                    )
                },
                autoDismiss = true
            ) {
                Column {
                    Text(text = text)
                }
            }
        }
    }
}