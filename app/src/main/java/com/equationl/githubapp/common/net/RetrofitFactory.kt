package com.equationl.githubapp.common.net

import com.equationl.githubapp.BuildConfig
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.logging.Level

object RetrofitFactory {
    // FIXME 注意这里的 token 拦截器有问题，如果 token 为空，登录后添加 token 将是无效的
    fun getOkhttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor("HttpLog").apply {
            setPrintLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC)
            setColorLevel(Level.INFO)
        }

        return OkHttpClient.Builder()
            .connectTimeout(AppConfig.HTTP_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(AppConfig.READ_TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(headerInterceptor())
            .addInterceptor(PageInfoInterceptor())
            .proxy(Proxy.NO_PROXY)
            .build()
    }

    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(AppConfig.GITHUB_API_BASE_URL)
            .client(okHttpClient)
            .build()
    }

    /**
     * 在请求头中增加 token
     * */
    private fun headerInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()

            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            if (token.isNotBlank()) {
                val accessToken = "token $token"
                val url = request.url.toString()
                request = request.newBuilder()
                    .addHeader("Authorization", accessToken)
                    .url(url)
                    .build()
            }

            chain.proceed(request)
        }
    }
}