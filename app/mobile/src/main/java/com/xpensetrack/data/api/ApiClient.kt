package com.xpensetrack.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // For emulator use: "http://10.0.2.2:8080/"
    // For physical device use your PC's local IP: "http://192.168.x.x:8080/"
    private const val BASE_URL = "http://192.168.1.60:8080/"
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
