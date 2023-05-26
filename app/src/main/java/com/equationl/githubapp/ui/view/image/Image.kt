@file:OptIn(ExperimentalCoilApi::class)

package com.equationl.githubapp.ui.view.image

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.equationl.githubapp.R
import com.equationl.githubapp.common.utlis.getImageLoader
import com.equationl.githubapp.common.utlis.savePictureFromCache
import com.equationl.githubapp.ui.common.PermissionDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val MinScale = 1f
private const val MaxScale = 10f

private var isRequestSuccess = false
private var cacheKey: String? = null

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImageScreen(
    image: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isAlreadyTap = remember { false }
    var imgSize: Size = remember { Size.Unspecified }

    var scale by remember { mutableStateOf(1f) }
    var offset  by remember { mutableStateOf(Offset.Zero) }

    val dialogState = rememberMaterialDialogState(false)
    val permissionDialogState = rememberMaterialDialogState(false)
    val dialogText = remember { mutableStateOf("保存至相册") }
    
    val permissionState = rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)


    val state =
        rememberTransformableState(onTransformation = { zoomChange, panChange, _ ->
            scale = (zoomChange * scale).coerceAtLeast(MinScale).coerceAtMost(MaxScale)
            offset = calOffset(imgSize, scale, offset + panChange)
        })

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.Black,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image)
                .placeholder(R.drawable.empty_img)
                .error(android.R.drawable.stat_notify_error)
                .build(),
            contentDescription = "previewImage",
            contentScale = ContentScale.Fit,
            onError = {
                Log.e("ImageScreen", "ImageScreen: ${it.result.throwable.stackTraceToString()}")
            },
            onSuccess = {
                isRequestSuccess = true
                cacheKey = it.result.diskCacheKey
                Log.i("el", "ImageScreen: cacheKey = $cacheKey")
            },
            imageLoader = context.getImageLoader(),
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = state)
                .graphicsLayer {
                    imgSize = size // 这个 size 是整个 AsyncImage 的 size 而非实际图像的 size
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale < 1f) {
                                scale = 1f
                            } else if (scale == 1f) {
                                scale = 2f
                            } else if (scale > 1f) {
                                scale = 1f
                            }

                            offset = Offset.Zero
                        },
                        onTap = {
                            if (!isAlreadyTap) {
                                isAlreadyTap = true
                                cacheKey = null
                                isRequestSuccess = false
                                navController.popBackStack()
                            }
                        },
                        onLongPress = {
                            if (isRequestSuccess) {
                                dialogState.show()
                            } else {
                                Log.i("el", "ImageScreen: request not success!")
                            }
                        }
                    )
                },
        )

        MaterialDialog(
            dialogState = dialogState,
            autoDismiss = false,
            onCloseRequest = {  }
        ) {
            Column {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        permissionDialogState.show()
                    }
                ) {
                    Text(text = dialogText.value)
                }
            }
        }

        PermissionDialog(
            permissionState = permissionState,
            dialogState = permissionDialogState
        ) {
            if (it) {
                savePicture(context, coroutineScope, dialogText, dialogState)
            }
            else {
                permissionDialogState.hide()
                dialogState.hide()
                Toast.makeText(context, "无权限！", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * 避免图片偏移超出屏幕
 * */
private fun calOffset(
    imgSize: Size,
    scale: Float,
    offset: Offset,
): Offset {
    if (imgSize == Size.Unspecified) return Offset.Zero
    val px = imgSize.width * (scale - 1f) / 2f
    val py = imgSize.height * (scale - 1f) / 2f
    var np = offset
    val xDiff = np.x.absoluteValue - px
    val yDiff = np.y.absoluteValue - py
    if (xDiff > 0)
        np = np.copy(x = px * np.x.absoluteValue / np.x)
    if (yDiff > 0)
        np = np.copy(y = py * np.y.absoluteValue / np.y)
    return np
}

private fun savePicture(
    context: Context,
    coroutineScope: CoroutineScope,
    dialogText: MutableState<String>,
    dialogState: MaterialDialogState
) {
    if (cacheKey != null) {
        coroutineScope.launch {
            try {
                dialogText.value = "正在保存中..."
                context.imageLoader.diskCache!!.openSnapshot(cacheKey!!)!!.use { snapShot ->
                    val imgFile = snapShot.data.toFile()
                    Log.i("el", "ImageScreen: imageFile = $imgFile")
                    val savedFile = savePictureFromCache(context, imgFile)
                    Log.i("el", "ImageScreen: 保存成功： ${savedFile.absolutePath}")
                    Toast.makeText(context, "保存成功！", Toast.LENGTH_SHORT).show()
                    snapShot.close()
                    dialogState.hide()
                    dialogText.value = "保存至相册"
                }
            } catch (tr: Throwable) {
                Log.e("el", "ImageScreen: ", tr)
                Toast.makeText(context, "保存图片失败:${tr.message}", Toast.LENGTH_SHORT).show()
                dialogState.hide()
            }

        }
    }
    else {
        Toast.makeText(context, "获取图片索引失败", Toast.LENGTH_SHORT).show()
        dialogState.hide()
    }
}