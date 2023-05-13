package com.equationl.githubapp.service

import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.model.bean.Notification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*


interface NotificationService {


    @GET("notifications")
    fun getNotification(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Query("all") all: Boolean,
            @Query("participating") participating: Boolean,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Notification>>

    @GET("notifications")
    fun getNotificationUnRead(
            @Header("forceNetWork") forceNetWork: Boolean,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = AppConfig.PAGE_SIZE
    ):Response<ArrayList<Notification>>

    @PATCH("notifications/threads/{threadId}")
    fun setNotificationAsRead(
            @Path("threadId") threadId: String):Response<ResponseBody>


    @PUT("notifications")
    fun setAllNotificationAsRead():Response<ResponseBody>

}
