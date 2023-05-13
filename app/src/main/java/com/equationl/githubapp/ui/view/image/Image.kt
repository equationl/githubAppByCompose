package com.equationl.githubapp.ui.view.image

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.githubapp.R

@Composable
fun ImageScreen(
    image: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                navController.popBackStack()
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .placeholder(R.drawable.empty_img)
                .error(android.R.drawable.stat_notify_error)
                .build(),
            contentDescription = "previewImage",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Fit,
            onError = {
                Log.e("ImageScreen", "ImageScreen: ${it.result.throwable.stackTraceToString()}")
            }
        )
    }
}