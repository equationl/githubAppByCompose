package com.equationl.githubapp.common.utlis

import okio.ByteString.Companion.decodeBase64

fun String?.formatReadme(
    fullPath: String
): String {
    return if (this.isNullOrEmpty()) {
        "**获取文件失败**"
    }

    else {
        var result = this.replace("\\n", "\n")
        result = (result.decodeBase64()?.string(Charsets.UTF_8)) ?: "**格式化文件失败**"
        result = formatImgPath(fullPath = fullPath, mdContent = result)
        result
    }
}

private fun formatImgPath(
    fullPath: String,
    mdContent: String
): String {
    var result = mdContent

    // TODO 在解析 MD 图片路径之前，应该先将 HTML 的图片转为 MD 的图片形式 <img src="https://github.com/guyijie1211/JustLive-Android/blob/master/pic/1.jpg" width="250" alt="首页推荐">

    try {
        val exp = Regex("!\\[[^]]*]\\((.*?)\\s*(\".*[^\"]\")?\\s*\\)")
        val tags = exp.findAll(result)
        for (m in tags) {
            val capture = m.groups[1]?.value
            if (capture != null && !(capture.startsWith("http://") || capture.startsWith("https://"))) {
                var newPath = getRealPath(
                    capture,
                    fullPath
                )
                newPath = newPath.replace("./", "")
                result = result.replace(capture, newPath)
            }
        }

        return result
    } catch (e: Exception) {
        e.printStackTrace()
        return result
    }
}

private fun getRealPath(rawPath: String, fullPath: String): String {
    val rawPathList = rawPath.split("../")
    val backCount = rawPathList.size - 1
    val rawContentPath = rawPathList[backCount]

    var realFullPath = fullPath

    repeat(backCount) {
        realFullPath = realFullPath.substring(0, realFullPath.lastIndexOf('/'))
    }

    return "$realFullPath/$rawContentPath"
}