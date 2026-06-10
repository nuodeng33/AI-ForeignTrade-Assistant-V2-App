package com.example.maolianzhihe.network

import com.example.maolianzhihe.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/local")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/local/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("deepseek/ask-trade-question")
    suspend fun askTradeQuestion(@Body request: DeepSeekRequest): Response<DeepSeekResponse>

    @GET("orders?sort=createdAt:desc")
    suspend fun getOrders(): Response<StrapiListResponse<Order>>

    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<StrapiResponse<Order>>

    companion object {
        fun getInstance(): ApiService = ApiClient.apiService
    }
}
