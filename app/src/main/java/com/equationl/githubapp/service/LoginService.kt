package com.equationl.githubapp.service

import com.equationl.githubapp.model.bean.AccessToken
import com.equationl.githubapp.model.bean.LoginRequestModel

import retrofit2.Response
import retrofit2.http.*

/**
 * 登录服务
 */
interface LoginService {

    @POST("authorizations")
    @Headers("Accept: application/json")
    fun authorizations(@Body authRequestModel: LoginRequestModel):Response<AccessToken>


    @GET("https://github.com/login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun authorizationsCode(@Query("client_id") client_id: String,
                           @Query("client_secret") client_secret: String,
                           @Query("code") code: String):Response<AccessToken>


}
