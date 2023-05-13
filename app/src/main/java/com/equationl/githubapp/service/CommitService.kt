package com.equationl.githubapp.service

import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.bean.CommitsComparison
import com.equationl.githubapp.model.bean.RepoCommit
import com.equationl.githubapp.model.bean.RepoCommitExt
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*


interface CommitService {

    @GET("repos/{owner}/{repo}/commits")
    suspend fun getRepoCommits(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            //SHA or branch to start listing commits from. Default: the repository’s default branch (usually master).
            @Query("page") page: Int,
            @Query("sha") branch: String = "master",
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ): Response<ArrayList<RepoCommit>>

    @GET("repos/{owner}/{repo}/commits/{sha}")
    suspend fun getCommitInfo(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("sha") sha: String
    ): Response<RepoCommitExt>

    @GET("repos/{owner}/{repo}/commits/{ref}/comments")
    fun getCommitComments(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Query("page") page: Int,
            @Path("ref") ref: String,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<RepoCommit>>

    @GET("repos/{owner}/{repo}/compare/{before}...{head}")
    fun compareTwoCommits(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Path("owner") owner: String,
            @Path("repo") repo: String,
            @Path("before") before: String,
            @Path("head") head: String
    ):Response<CommitsComparison>

}
