package com.equationl.githubapp.common.utlis

import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.ui.graphics.Color
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.util.Utils.toHexString
import com.equationl.githubapp.util.fromJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * 通用工具类
 */
object CommonUtils {

    private const val MILLIS_LIMIT = 1000.0

    private const val SECONDS_LIMIT = 60 * MILLIS_LIMIT

    private const val MINUTES_LIMIT = 60 * SECONDS_LIMIT

    private const val HOURS_LIMIT = 24 * MINUTES_LIMIT

    private const val DAYS_LIMIT = 30 * HOURS_LIMIT


    fun getDateStr(date: Date?): String {
        if (date?.toString() == null) {
            return ""
        } else if (date.toString().length < 10) {
            return date.toString()
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date).substring(0, 10)
    }

    /**
     * 获取时间格式化
     */
    fun getNewsTimeStr(date: Date?): String {
        if (date == null) {
            return ""
        }
        val subTime = Date().time - date.time
        return when {
            subTime < MILLIS_LIMIT -> "刚刚"
            subTime < SECONDS_LIMIT -> Math.round(subTime / MILLIS_LIMIT).toString() + " " + "秒前"
            subTime < MINUTES_LIMIT -> Math.round(subTime / SECONDS_LIMIT).toString() + " " + "分钟前"
            subTime < HOURS_LIMIT -> Math.round(subTime / MINUTES_LIMIT).toString() + " " + "小时前"
            subTime < DAYS_LIMIT -> Math.round(subTime / HOURS_LIMIT).toString() + " " + "天前"
            else -> getDateStr(date)
        }
    }


    fun getReposHtmlUrl(userName: String, reposName: String): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName

    fun getIssueHtmlUrl(userName: String, reposName: String, number: String): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/issues/" + number

    fun getUserHtmlUrl(userName: String) =
            AppConfig.GITHUB_BASE_URL + userName

    fun getFileHtmlUrl(userName: String, reposName: String, path: String, branch: String = "master"): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/blob/" + branch + if (path.startsWith("/")) path else "/$path"

    fun getCommitHtmlUrl(userName: String, reposName: String, sha: String): String =
            AppConfig.GITHUB_BASE_URL + userName + "/" + reposName + "/commit/" + sha

    private val sImageEndTag = arrayListOf(".png", ".jpg", ".jpeg", ".gif", ".svg")

    fun isImageEnd(path: String): Boolean {
        var image = false
        sImageEndTag.forEach {
            if (path.indexOf(it) + it.length == path.length) {
                image = true
            }
        }
        return image
    }

    /**
     * 获取用户贡献图 URL
     * */
    fun getUserChartAddress(name: String, color: Color): String {
        return "${AppConfig.GRAPHIC_HOST}${color.toHexString}/$name"
    }

    /**
     *  获取用户的 star 数量，并更新到 user 中
     * */
    suspend fun updateStar(user: User, repoService: RepoService, ) {
        val startResponse = repoService.getStarredRepos(true, user.login ?: "", 1, "updated", 1)
        val honorResponse = repoService.getUserRepository100StatusDao(true, user.login ?: "", 1)
        val starCount = startResponse.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1
        if (starCount != -1) {
            user.starRepos = starCount
        }

        if (honorResponse.isSuccessful) {
            val list = honorResponse.body()
            var count = 0
            list?.forEach {
                count += it.watchersCount
            }
            user.honorRepos = count
        }
    }

    fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().flush()
    }

}