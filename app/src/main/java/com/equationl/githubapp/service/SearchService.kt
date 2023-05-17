package com.equationl.githubapp.service

import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.model.bean.Repository
import com.equationl.githubapp.model.bean.SearchResult
import com.equationl.githubapp.model.bean.User

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


interface SearchService {

    @GET("search/users")
    suspend fun searchUsers(
            @Query(value = "q", encoded = true) query: String,
            @Query("sort") sort: String = "best%20match",
            @Query("order") order: String = "desc",
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<SearchResult<User>>

    @GET("search/repositories")
    suspend fun searchRepos(
            @Query(value = "q", encoded = true) query: String,
            @Query("sort") sort: String = "best%20match",
            @Query("order") order: String = "desc",
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<SearchResult<Repository>>

    @GET("search/issues")
    @Headers("Accept: application/vnd.github.html,application/vnd.github.VERSION.raw")
    suspend fun searchIssues(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Query(value = "q", encoded = true) query: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<SearchResult<Issue>>

}
