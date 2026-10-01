package com.example.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configured Retrofit client providing singleton access to KuroApiService
 * with support for dynamic runtime base URL switching from the Admin Panel.
 */
object RetrofitClient {

    private const val DEFAULT_BASE_URL = "https://api.kurostream.app/"
    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    @Volatile
    private var cachedService: KuroApiService? = null

    @Volatile
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun getActiveBaseUrl(): String = currentBaseUrl

    /**
     * Dynamically updates the active API Base URL configured in the Admin Panel.
     */
    fun setActiveBaseUrl(newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        currentBaseUrl = formatted
        cachedService = createService(formatted)
    }

    /**
     * Configured Moshi JSON parser instance with Kotlin reflection support.
     */
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Request headers interceptor adding standard API headers and authorization tokens.
     */
    private val headersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "KuroStream-Android/2.0")
            .header("X-Client-Platform", "Android")

        authToken?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    /**
     * Logging interceptor for debugging network calls and HLS streaming authorizations.
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    /**
     * Configured OkHttpClient with custom timeouts and interceptors.
     */
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Returns the active dynamically-configured KuroApiService.
     */
    val apiService: KuroApiService
        get() = cachedService ?: synchronized(this) {
            cachedService ?: createService(currentBaseUrl).also { cachedService = it }
        }

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
     * Creates a custom configured KuroApiService with a specific base URL.
     */
    fun createService(baseUrl: String): KuroApiService {
        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KuroApiService::class.java)
    }
}
