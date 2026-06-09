package com.example.maolianzhihe.network

import com.example.maolianzhihe.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {
    // 用户认证
    @POST("auth/local")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/local/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // DeepSeek
    @POST("deepseek/ask-trade-question")
    suspend fun askTradeQuestion(@Body request: DeepSeekRequest): Response<DeepSeekResponse>

    // 获取订单列表 - 适配 Strapi 格式
    @GET("orders?sort=createdAt:desc")
    suspend fun getOrders(): Response<StrapiListResponse<Order>>

    // 创建订单 - 适配 Strapi 格式
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<StrapiResponse<Order>>
    companion object {
        private var INSTANCE: ApiService? = null

        fun getInstance(): ApiService {
            if (INSTANCE == null) {
                val baseUrl = "http://192.168.1.3:1337/api/"

                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                INSTANCE = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return INSTANCE!!
        }
    }
}