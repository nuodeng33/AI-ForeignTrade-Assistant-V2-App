package com.example.maolianzhihe.network

import com.example.maolianzhihe.BuildConfig
import com.example.maolianzhihe.data.local.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        SessionManager.token()?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }
        val response = chain.proceed(builder.build())
        if (response.code == 401) {
            SessionManager.clear()
        }
        response
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val streamingClient: OkHttpClient = okHttpClient.newBuilder()
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun resolveUrl(path: String): String {
        return BuildConfig.API_BASE_URL.toHttpUrl()
            .newBuilder()
            .addPathSegments(path.trimStart('/'))
            .build()
            .toString()
    }
}
