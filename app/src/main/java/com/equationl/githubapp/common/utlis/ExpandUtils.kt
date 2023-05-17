package com.equationl.githubapp.common.utlis

import android.content.ClipData
import android.content.Context
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * 复制到剪切板
 */
fun Context.copy(string: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE)
            as android.content.ClipboardManager
    val clip = ClipData.newPlainText("", string)
    clipboardManager.setPrimaryClip(ClipData(clip))
}

/**
 * 获取版本号
 */
fun Context.getVersionName(): String {
    @Suppress("DEPRECATION")
    val manager = packageManager.getPackageInfo(packageName, 0)
    return manager.versionName
}


/**
 * 列表到文本转化
 */
fun List<String>.toSplitString(): String {
    var result = ""
    this.forEach {
        result = "$result/$it"
    }
    return result.replace("./", "")
}

/**
 * 版本号对比
 */
fun String.compareVersion(v2: String?): String? {
    if (v2.isNullOrEmpty()) return null
    val regEx = "[^0-9]"
    val p = Pattern.compile(regEx)
    var s1: String = p.matcher(this).replaceAll("").trim()
    var s2: String = p.matcher(v2).replaceAll("").trim()

    val cha: Int = s1.length - s2.length
    val buffer = StringBuffer()
    var i = 0
    while (i < abs(cha)) {
        buffer.append("0")
        ++i
    }

    if (cha > 0) {
        buffer.insert(0, s2)
        s2 = buffer.toString()
    } else if (cha < 0) {
        buffer.insert(0, s1)
        s1 = buffer.toString()
    }

    val s1Int = s1.toInt()
    val s2Int = s2.toInt()

    return if (s1Int > s2Int) this
    else v2
}