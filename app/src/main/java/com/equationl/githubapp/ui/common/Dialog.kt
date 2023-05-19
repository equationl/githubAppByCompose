package com.equationl.githubapp.ui.common

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.equationl.githubapp.model.ui.EventUIAction
import com.equationl.githubapp.model.ui.EventUIModel
import com.vanpra.composematerialdialogs.MaterialDialog

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