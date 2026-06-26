package com.factlens.network

import com.factlens.model.VerificationRequest
import com.factlens.model.VerificationResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface FactLensApi {

    @POST("verify")
    fun verifyText(@Body request: VerificationRequest): Call<VerificationResponse>

    companion object {
        // Use 10.0.2.2 for Android emulator, change for real device
        private const val BASE_URL = "http://10.0.2.2:8000/"

        fun create(): FactLensApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FactLensApi::class.java)
        }
    }
}
