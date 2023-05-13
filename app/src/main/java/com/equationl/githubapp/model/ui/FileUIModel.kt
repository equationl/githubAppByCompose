package com.equationl.githubapp.model.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.ui.graphics.vector.ImageVector


/**
 * 文件相关UI类型
 */
data class FileUIModel(
    var icon: ImageVector = Icons.Filled.Circle,
    var title: String = "",
    var next: String = "",
    var type: String = "",
    var dir: String = "",
    var patch: String = "",
    var sha: String = ""
)