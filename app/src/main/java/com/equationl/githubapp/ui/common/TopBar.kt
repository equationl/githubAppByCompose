package com.equationl.githubapp.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.equationl.githubapp.ui.view.main.MainPager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, navigationIcon: ImageVector = Icons.Filled.ArrowBack, actions: @Composable RowScope.() -> Unit = {}, onBack: () -> Unit) {
    TopAppBar (
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(navigationIcon, "返回")
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(title: String, navigationIcon: ImageVector, mainPager: MainPager, actions: @Composable RowScope.() -> Unit = {}, onBack: () -> Unit) {
    TopAppBar (
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            /*AnimatedContent(
                targetState = title,
                transitionSpec = {
                    if (currentPager == CurrentPager.HOME_ME) {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    }
                    else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                }
            ) { targetTitle ->
                Text(text = targetTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }*/
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(navigationIcon, "返回")
            }
        },
        actions = actions
    )
}

@Composable
fun MoreMenu(
    isShow: Boolean,
    options: List<String> = listOf("在浏览器中打开", "复制链接", "分享"),
    onDismissRequest: () -> Unit,
    onClick: (pos: Int) -> Unit
) {
    DropdownMenu(expanded = isShow, onDismissRequest = onDismissRequest) {
        options.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {
                    Text(text = item)
                },
                onClick = {
                    onDismissRequest()
                    onClick(index)
                },
            )
        }
    }
}