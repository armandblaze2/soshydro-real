package com.example.data.remote

import com.example.data.model.AuthRequest
import com.example.data.model.UpdateDetaillantRequest
import com.example.data.model.UpdateStationRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface SosHydroApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "ngrok-skip-browser-warning: true"
    )
    @POST("api_fournisseur.php")
    suspend fun auth(@Body request: AuthRequest): Response<ResponseBody>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "ngrok-skip-browser-warning: true"
    )
    @POST("api_fournisseur.php")
    suspend fun updateStation(@Body request: UpdateStationRequest): Response<ResponseBody>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "ngrok-skip-browser-warning: true"
    )
    @POST("api_fournisseur.php")
    suspend fun updateDetaillant(@Body request: UpdateDetaillantRequest): Response<ResponseBody>

    companion object {
        private const val BASE_URL = "https://placard-elevator-five.ngrok-free.dev/drive/"

        fun create(): SosHydroApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SosHydroApiService::class.java)
        }
    }
}
