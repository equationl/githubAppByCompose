package com.equationl.githubapp.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Locale


object Utils {
    /**
     * 将 [Color] 转为 十六进制字符串，并舍弃 Alpha 通道，返回字符串不包括 *#*
     * */
    val Color.toHexString
        get() = Integer.toHexString(this.toArgb()).substring(2).uppercase(Locale.CHINA)

    /**
     * 将 [Color] 转为 *#* 开头的十六进制字符串，并舍弃 Alpha 通道
     * */
    val Color.toString
        get() = "#" + this.toHexString
}