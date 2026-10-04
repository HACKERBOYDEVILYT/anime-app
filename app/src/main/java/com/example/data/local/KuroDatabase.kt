package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AdminScrapedDao
import com.example.data.local.dao.CommentsDao
import com.example.data.local.dao.DownloadsDao
import com.example.data.local.dao.MalSyncDao
import com.example.data.local.dao.SocialDao
import com.example.data.local.dao.WatchDao
import com.example.data.local.dao.WatchlistDao
import com.example.data.local.entity.ApiEndpointEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.EpisodeCommentEntity
import com.example.data.local.entity.MalSyncEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ReviewEntity
import com.example.data.local.entity.ScrapedVideoEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.local.entity.WatchHistoryEntity
import com.example.data.local.entity.WatchlistEntity

@Database(
    entities = [
        WatchHistoryEntity::class,
        WatchlistEntity::class,
        ReviewEntity::class,
        NotificationEntity::class,
        DownloadEntity::class,
        EpisodeCommentEntity::class,
        MalSyncEntity::class,
        ScrapedVideoEntity::class,
        UserAccountEntity::class,
        ApiEndpointEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KuroDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun socialDao(): SocialDao
    abstract fun downloadsDao(): DownloadsDao
    abstract fun commentsDao(): CommentsDao
    abstract fun malSyncDao(): MalSyncDao
    abstract fun adminScrapedDao(): AdminScrapedDao

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
