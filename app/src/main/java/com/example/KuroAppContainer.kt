package com.example

import android.content.Context
import com.example.data.local.KuroDatabase
import com.example.data.network.KuroApiService
import com.example.data.network.NetworkClient
import com.example.data.network.RetrofitClient
import com.example.data.repository.AdminRepository
import com.example.data.repository.AnimeRepository
import com.example.data.repository.CommentsRepository
import com.example.data.repository.DownloadsRepository
import com.example.data.repository.LocalLicensedMediaProvider
import com.example.data.repository.MalSyncRepository
import com.example.data.repository.RetrofitMetadataProvider
import com.example.data.repository.ScheduleRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchRepository

class KuroAppContainer(context: Context) {
    private val database = KuroDatabase.getInstance(context)

    val apiService: KuroApiService = RetrofitClient.apiService
    val localMediaProvider = LocalLicensedMediaProvider()
    val metadataProvider = RetrofitMetadataProvider(apiService, localMediaProvider)

    val animeRepository = AnimeRepository(metadataProvider)
    val watchRepository = WatchRepository(
        watchDao = database.watchDao(),
        watchlistDao = database.watchlistDao(),
        socialDao = database.socialDao()
    )
    val userRepository = UserRepository()
    val adminRepository = AdminRepository(localMediaProvider)
    val downloadsRepository = DownloadsRepository(database.downloadsDao())
    val commentsRepository = CommentsRepository(database.commentsDao())
    val malSyncRepository = MalSyncRepository(database.malSyncDao())
    val scheduleRepository = ScheduleRepository(animeRepository)

    companion object {
        @Volatile
        private var INSTANCE: KuroAppContainer? = null

        fun getInstance(context: Context): KuroAppContainer {
            return INSTANCE ?: synchronized(this) {
                val instance = KuroAppContainer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
