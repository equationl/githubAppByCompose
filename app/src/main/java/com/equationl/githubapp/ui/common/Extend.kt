package com.equationl.githubapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

inline fun Modifier.noRippleClickable(crossinline onClick: ()->Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun Modifier.comPlaceholder(
    isRefresh: Boolean
): Modifier = composed {
    placeholder(
        visible = isRefresh,
        highlight = PlaceholderHighlight.fade(),
        color = Color.Unspecified // MaterialTheme.colorScheme.onSurfaceVariant
    )
}