package com.xpensetrack.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Production API
    private const val BASE_URL = "https://xpensetrack-4fdf.onrender.com/"
    
    // For local development:
    // Emulator: "http://10.0.2.2:8080/"
    // Physical device: "http://192.168.1.60:8080/"
    var token: String? = null

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
        token?.let { req.addHeader("Authorization", "Bearer $it") }
        chain.proceed(req.build())
    }

    private val client = OkHttpClient.Builder().addInterceptor(authInterceptor).build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
