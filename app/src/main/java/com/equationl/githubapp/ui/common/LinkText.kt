package com.equationl.githubapp.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun LinkText(
    text: String,
    textDecoration: TextDecoration = TextDecoration.Underline,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit) {

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        textDecoration = textDecoration,
        modifier = modifier.noRippleClickable(onClick = onClick)
    )
}