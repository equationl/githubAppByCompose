package com.equationl.githubapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.string.RichTextStringStyle

@Composable
fun getRichTextStyle(): RichTextStyle {
    val defaultRichTextStyle = RichTextStyle.Default
    val codeBlockStyle = CodeBlockStyle.Default
    val codeTextStyle = codeBlockStyle.textStyle ?: TextStyle.Default
    val stringStyle = RichTextStringStyle.Default
    val linkStyle = stringStyle.linkStyle ?: SpanStyle()
    val codeStyle = stringStyle.codeStyle ?: SpanStyle()

    return defaultRichTextStyle.copy(
        codeBlockStyle = codeBlockStyle.copy(
            modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.inverseSurface),
            textStyle = codeTextStyle.copy(color = MaterialTheme.colorScheme.inverseOnSurface),
            wordWrap = false
        ),
        stringStyle = stringStyle.copy(
            linkStyle = linkStyle.copy(color = MaterialTheme.colorScheme.primary),
            codeStyle = codeStyle.copy(color = MaterialTheme.colorScheme.inverseOnSurface, background = MaterialTheme.colorScheme.inverseSurface)
        )
    )
}