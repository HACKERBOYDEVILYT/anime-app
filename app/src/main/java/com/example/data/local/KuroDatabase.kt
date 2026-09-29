package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SocialDao
import com.example.data.local.dao.WatchDao
import com.example.data.local.dao.WatchlistDao
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.WatchHistoryEntity
import com.example.data.local.entity.WatchlistEntity

@Database(
    entities = [
        WatchHistoryEntity::class,
        WatchlistEntity::class,
        ReviewEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KuroDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun socialDao(): SocialDao

    companion object {
        @Volatile
        private var INSTANCE: KuroDatabase? = null

        fun getInstance(context: Context): KuroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KuroDatabase::class.java,
                    "kuro_stream_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
