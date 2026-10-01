package com.example.data.network

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient

/**
 * Backward-compatible network client facade delegating to [RetrofitClient].
 */
object NetworkClient {

    val moshi: Moshi
        get() = RetrofitClient.moshi

    val okHttpClient: OkHttpClient
        get() = RetrofitClient.okHttpClient

    fun createService(baseUrl: String = "https://api.kurostream.app/"): KuroApiService {
        return RetrofitClient.createService(baseUrl)
    }
}
