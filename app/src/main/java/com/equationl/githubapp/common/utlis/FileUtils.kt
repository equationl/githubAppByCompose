package com.equationl.githubapp.common.utlis

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun imgCachePath(context: Context) = context.cacheDir.resolve("image_cache")

fun savePicturePath(): File = run {
    val rootFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    File(rootFile, "GithubApp/")
}

suspend fun savePictureFromCache(context: Context, cache: File): File {
    return withContext(Dispatchers.IO) {
        val saveName = "${System.currentTimeMillis()}-GithubAppByEl.jpg"
        val targetFile = File(savePicturePath(), saveName)
        val savedFile = cache.copyTo(targetFile)

        MediaScannerConnection.scanFile(context, arrayOf(savedFile.absolutePath), null, null)

        savedFile
    }
}