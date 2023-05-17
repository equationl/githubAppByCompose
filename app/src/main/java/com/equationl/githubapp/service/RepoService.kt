package com.equationl.githubapp.service

import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.bean.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*


interface RepoService {

    @GET("users/{user}/repos")
    suspend fun getUserRepository100StatusDao(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("sort") sort: String = "pushed",
            @Query("per_page") per_page: Int = 100
    ): Response<ArrayList<Repository>>


    @GET("users/{user}/starred")
    suspend fun getStarredRepos(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("sort") sort: String = "updated",
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Repository>>

    @GET("user/repos")
    fun getUserRepos(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Query("page") page: Int,
            @Query("type") type: String,
            @Query("sort") sort: String,
            @Query("direction") direction: String,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Repository>>

    /**
     * List user repositories
     */
    @GET("users/{user}/repos")
    suspend fun getUserPublicRepos(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("user") user: String,
            @Query("page") page: Int,
            @Query("sort") sort: String = "pushed",
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):retrofit2.Response<ArrayList<Repository>>

    /**
     * Check if you are starring a repository
     */
    @GET("user/starred/{owner}/{repo}")
    suspend fun checkRepoStarred(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ResponseBody>

    /**
     * Star a repository
     */
    @PUT("user/starred/{owner}/{repo}")
    suspend fun starRepo(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ResponseBody>

    /**
     * Unstar a repository
     */
    @DELETE("user/starred/{owner}/{repo}")
    suspend fun unstarRepo(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ResponseBody>

    @GET("user/subscriptions/{owner}/{repo}")
    suspend fun checkRepoWatched(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ResponseBody>

    @PUT("user/subscriptions/{owner}/{repo}")
    suspend fun watchRepo(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ResponseBody>

    @DELETE("user/subscriptions/{owner}/{repo}")
    suspend fun unwatchRepo(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ResponseBody>

    @GET
    @Headers("Accept: application/vnd.github.html")
    fun getFileAsHtmlStream(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Url url: String
    ):Response<ResponseBody>

    @GET
    @Headers("Accept: application/vnd.github.VERSION.raw")
    fun getFileAsStream(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Url url: String
    ):Response<ResponseBody>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getRepoFiles(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("path", encoded = true) path: String,
    ):Response<ArrayList<FileModel>>

    @GET("repos/{owner}/{repo}/branches")
    fun getBranches(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ArrayList<Branch>>

    @GET("repos/{owner}/{repo}/tags")
    fun getTags(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<ArrayList<Branch>>

    @GET("repos/{owner}/{repo}/stargazers")
    suspend fun getStargazers(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path(value = "owner") owner: String,
            @Path(value = "repo") repo: String,
            @Query("page") page: Int
    ):Response<ArrayList<User>>

    @GET("repos/{owner}/{repo}/subscribers")
    suspend fun getWatchers(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int
    ):Response<ArrayList<User>>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepoInfo(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<Repository>

    @POST("repos/{owner}/{repo}/forks")
    suspend fun createFork(
            @Path("owner") owner: String,
            @Path("repo") repo: String
    ):Response<Repository>

    @GET("repos/{owner}/{repo}/forks")
    suspend fun getForks(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Repository>>

    /**
     * List public events for a network of repositories
     */
    @GET("networks/{owner}/{repo}/events")
    suspend fun getRepoEvent(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Event>>

    @GET("repos/{owner}/{repo}/releases")
    @Headers("Accept: application/vnd.github.html")
    fun getReleases(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Release>>


    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleasesNotHtml(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Release>>


    @GET("repos/{owner}/{repo}/releases/tags/{tag}")
    @Headers("Accept: application/vnd.github.html")
    fun getReleaseByTagName(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("tag") tag: String
    ):Response<Release>


    @GET("https://github.com/trending/{languageType}")
    @Headers("Content-Type: text/plain;charset=utf-8")
    fun getTrendData(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("languageType") languageType: String,
            @Query("since") since: String):Response<String>

    @GET("https://guoshuyu.cn/github/trend/list")
    @Headers("Content-Type: text/plain;charset=utf-8")
    suspend fun getTrendDataAPI(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Header("api-token") apiToken: String,
            @Query("since") since: String,
            @Query("languageType") languageType: String
    ):Response<List<TrendingRepoModel>>

    @GET("repos/{owner}/{repo}/readme")
    @Headers("Content-Type: text/plain;charset=utf-8", "Accept: application/vnd.github.html")
    suspend fun getReadmeHtml(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,):Response<String>


    @GET("repos/{owner}/{repo}/contents/{path}")
    @Headers("Content-Type: text/plain;charset=utf-8", "Accept: application/vnd.github.html")
    suspend fun getRepoFilesDetail(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path(value = "path", encoded = true) path: String,
    ):Response<String>

}
