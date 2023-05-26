package com.equationl.githubapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.equationl.githubapp.R
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.getImageLoader
import com.equationl.githubapp.model.ui.FileUIModel
import com.equationl.githubapp.model.ui.ReposUIModel
import com.ireward.htmlcompose.HtmlText

@Composable
fun VerticalIconText(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = text, tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
        Text(text = text, color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileItem(
    fileUiModel: FileUIModel,
    onClickFileItem: (fileUiModel: FileUIModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (fileUiModel.dir.isNotBlank()) {
            Text(text = fileUiModel.dir, modifier = Modifier.padding(horizontal = 5.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (fileUiModel.dir.isBlank()) 0.dp else 10.dp),
            onClick = { onClickFileItem(fileUiModel) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                Icon(imageVector = fileUiModel.icon, contentDescription = "File")
                Text(text = fileUiModel.title)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoItem(
    data: ReposUIModel,
    isRefresh: Boolean,
    navController: NavHostController,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = data.ownerPic,
                        navHostController = navController,
                        userName = data.ownerName,
                        isRefresh = isRefresh
                    )

                    Column(
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = data.repositoryName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.comPlaceholder(isRefresh)
                        )
                        Row {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.comPlaceholder(isRefresh))
                            Text(text = data.ownerName, modifier = Modifier.comPlaceholder(isRefresh))
                        }
                    }
                }

                Text(text = data.repositoryType, modifier = Modifier.comPlaceholder(isRefresh))
            }

            HtmlText(
                text = data.repositoryDes,
                modifier = Modifier.comPlaceholder(isRefresh)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                VerticalIconText(icon = Icons.Filled.StarBorder, text = data.repositoryStar, modifier = Modifier.comPlaceholder(isRefresh))
                VerticalIconText(icon = Icons.Filled.Share, text = data.repositoryFork, modifier = Modifier.comPlaceholder(isRefresh))
                VerticalIconText(icon = Icons.Filled.Visibility, text = data.repositoryWatch, modifier = Modifier.comPlaceholder(isRefresh))
            }
        }
    }
}

@Composable
fun AvatarContent(
    data: Any,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(30.dp, 30.dp),
    isRefresh: Boolean? = null,
    isCircle: Boolean = true,
    navHostController: NavHostController? = null,
    userName: String? = null,
    onClick: (() -> Unit)? = null,
) {
    var realModifier = modifier.size(size)

    if (isCircle) realModifier = realModifier.clip(CircleShape)
    realModifier = if (onClick == null) {
        realModifier.clickable {
            navHostController?.navigate("${Route.PERSON_DETAIL}/$userName")
        }
    }
    else {
        realModifier.clickable(onClick = onClick)
    }

    if (isRefresh != null) {
        realModifier = realModifier.comPlaceholder(isRefresh)
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .placeholder(R.drawable.empty_img)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = "avatar",
        modifier = realModifier,
        imageLoader = LocalContext.current.getImageLoader()
    )
}

@Composable
fun CheckBoxGroup(
    options: List<String>,
    defaultCheck: Int,
    onCheckChange: (index: Int) -> Unit,
) {
    var checkIndex by remember { mutableStateOf(defaultCheck) }
    // checkIndex = defaultCheck

    Column {
        options.forEachIndexed { index, s ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (checkIndex == index) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background)
                    .clickable {
                        checkIndex = index
                        onCheckChange(index)
                    }
            ) {
                Checkbox(checked = checkIndex == index, onCheckedChange = {})
                Text(text = s, color = if (checkIndex == index) MaterialTheme.colorScheme.onSecondary else Color.Unspecified)
            }
        }
    }
}

@Composable
fun EmptyItem(
    isNotInit: Boolean = false,
    text: String? = null
) {
    if (isNotInit) {
        Text(text = "暂时没有任何数据哦~~")
    }
    else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_result))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )

            Text(text = if (text.isNullOrEmpty()) "暂时没有任何数据哦" else text)
        }
    }
}

@Composable
fun LoadItem(text: String? = null) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )

        Text(text = if (text.isNullOrEmpty()) "加载中……" else text)
    }
}