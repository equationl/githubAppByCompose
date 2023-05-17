package com.equationl.githubapp.service

import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.bean.CommentRequestModel
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.model.bean.IssueEvent
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*


interface IssueService {

    @GET("repos/{owner}/{repo}/issues")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun getRepoIssues(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int,
            @Query("state") state: String = "all",
            @Query("sort") sort: String = "created",
            @Query("direction") direction: String = "desc",
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Issue>>

    @GET("user/issues")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    fun getUserIssues(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Query("filter") filter: String,
            @Query("state") state: String,
            @Query("sort") sort: String,
            @Query("direction") direction: String,
            @Query("page") page: Int
    ):Response<ArrayList<Issue>>

    @GET("repos/{owner}/{repo}/issues/{issueNumber}")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun getIssueInfo(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int
    ):Response<Issue>

    @GET("repos/{owner}/{repo}/issues/{issueNumber}/timeline")
    @Headers("Accept: application/vnd.github.mockingbird-preview")
    fun getIssueTimeline(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int,
            @Query("page") page: Int
    ):Response<ArrayList<IssueEvent>>

    @GET("repos/{owner}/{repo}/issues/{issueNumber}/comments")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun getIssueComments(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ): Response<ArrayList<IssueEvent>>

    @GET("repos/{owner}/{repo}/issues/{issueNumber}/events")
    @Headers("Accept: application/vnd.github.html")
    fun getIssueEvents(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int,
            @Query("page") page: Int
    ):Response<ArrayList<IssueEvent>>

    @POST("repos/{owner}/{repo}/issues/{issueNumber}/comments")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun addComment(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int,
            @Body body: CommentRequestModel
    ):Response<IssueEvent>

    @PATCH("repos/{owner}/{repo}/issues/comments/{commentId}")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun editComment(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("commentId") commentId: String,
            @Body body: CommentRequestModel
    ):Response<IssueEvent>

    @DELETE("repos/{owner}/{repo}/issues/comments/{commentId}")
    suspend fun deleteComment(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("commentId") commentId: String
    ):Response<ResponseBody>

    @PATCH("repos/{owner}/{repo}/issues/{issueNumber}")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun editIssue(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int,
            @Body body: Issue
    ):Response<Issue>

    @POST("repos/{owner}/{repo}/issues")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun createIssue(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Body body: Issue
    ):Response<Issue>

    @PUT("repos/{owner}/{repo}/issues/{issueNumber}/lock")
    suspend fun lockIssue(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int
    ):Response<ResponseBody>

    @DELETE("repos/{owner}/{repo}/issues/{issueNumber}/lock")
    suspend fun unLockIssue(
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("issueNumber") issueNumber: Int
    ):Response<ResponseBody>
}
