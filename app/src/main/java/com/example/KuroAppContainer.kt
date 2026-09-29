package com.example

import android.content.Context
import com.example.data.local.KuroDatabase
import com.example.data.repository.AdminRepository
import com.example.data.repository.AnimeRepository
import com.example.data.repository.LocalLicensedMediaProvider
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchRepository

class KuroAppContainer(context: Context) {
    private val database = KuroDatabase.getInstance(context)

    val mediaProvider = LocalLicensedMediaProvider()
    val animeRepository = AnimeRepository(mediaProvider)
    val watchRepository = WatchRepository(
        watchDao = database.watchDao(),
        watchlistDao = database.watchlistDao(),
        socialDao = database.socialDao()
    )
    val userRepository = UserRepository()
    val adminRepository = AdminRepository(mediaProvider)

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
