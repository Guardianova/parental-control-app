package com.guardianova.child.core.network

import android.content.Context
import com.guardianova.child.core.storage.EncryptedStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // ⚠️ عدّل هذا الرابط لرابط Backend الفعلي على Replit
    private const val BASE_URL = "https://CHANGE-ME.repl.co"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun build(context: Context): Retrofit {
        val storage = EncryptedStorage(context)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .apply {
                        val token = storage.getAccessToken()
                        if (token != null) {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
