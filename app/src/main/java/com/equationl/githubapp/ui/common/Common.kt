package com.equationl.githubapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.githubapp.R
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.FileUIModel

@Composable
fun VerticalIconText(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = text)
        Text(text = text)
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

@Composable
fun AvatarContent(
    data: Any,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(30.dp, 30.dp),
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

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .placeholder(R.drawable.empty_img)
            .build(),
        contentDescription = "avatar",
        modifier = realModifier
    )
}

@Composable
fun EmptyItem() {
    Text(text = "暂时没有任何数据哦～")
}