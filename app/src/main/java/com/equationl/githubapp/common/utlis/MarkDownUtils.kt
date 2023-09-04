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

/**
 * 格式化图片路径：
 *
 * 1. 将相对路径格式转为绝对路径。
 *
 * 2. 将 HTML 的 img 标签替换为 MD 的图片格式
 *
 * 3. 将 https://github.com/ 开头的图片路径替换为 https://raw.githubusercontent.com/ 并移除 blob 路径，
 * 如 https://github.com/equationl/sampleRepo/blob/master/pic/1.jpg 替换为 https://raw.githubusercontent.com/equationl/sampleRepo/master/pic/1.jpg
 * */
private fun formatImgPath(
    fullPath: String,
    mdContent: String
): String {
    var result = mdContent.imgTag2MdImg()

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
            else if (capture != null && capture.startsWith("https://github.com/")) { // fixme 这里可能会有问题
                result = result.replace(capture, capture.replace("https://github.com/", "https://raw.githubusercontent.com/").replace("/blob", ""))
            }
        }

        return result
    } catch (e: Exception) {
        e.printStackTrace()
        return result
    }
}

/**
 * 将 HTML 的图片标签 <img src="xxx"> 转为 MD 的图片标签 ![xxx](xxx)
 * */
private fun String.imgTag2MdImg(): String {
    var result = this

    val fullTextRegex = Regex("<img[\\s\\S]*?>")
    val fullTextTags = fullTextRegex.findAll(this)
    for (fullTextTag in fullTextTags) {
        val tag = fullTextTag.value

        var src = Regex("<img.*?src=\"(.*?)\"").find(tag)?.groupValues?.getOrNull(1) ?: ""
        if (src.isNotBlank()) {
            src = "![img]($src) \n\n"
        }
        result = result.replace(tag, src)
    }

    return result
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