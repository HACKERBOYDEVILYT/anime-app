package com.example

import android.content.Context
import com.example.data.local.KuroDatabase
import com.example.data.network.CatalogNetworkMonitor
import com.example.data.network.RetrofitClient
import com.example.data.repository.AdminRepository
import com.example.data.repository.AnimeRepository
import com.example.data.repository.AppUpdateRepository
import com.example.data.repository.CommentsRepository
import com.example.data.repository.DownloadsRepository
import com.example.data.repository.GamificationAndSocialRepository
import com.example.data.repository.LocalLicensedMediaProvider
import com.example.data.repository.MalSyncRepository
import com.example.data.repository.QuotesRepository
import com.example.data.repository.RetrofitMetadataProvider
import com.example.data.repository.ScheduleRepository
import com.example.data.repository.TierListRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchPartyRepository
import com.example.data.repository.WatchRepository
import com.example.data.sync.CloudSyncManager

class KuroAppContainer(context: Context) {

    private val database: KuroDatabase = KuroDatabase.getInstance(context)

    val catalogNetworkMonitor: CatalogNetworkMonitor = CatalogNetworkMonitor.getInstance(context)

    val cloudSyncManager: CloudSyncManager = CloudSyncManager()

    val mediaProvider: LocalLicensedMediaProvider = LocalLicensedMediaProvider(
        adminScrapedDao = database.adminScrapedDao()
    )

    private val remoteMetadataProvider = RetrofitMetadataProvider(
        apiService = RetrofitClient.apiService,
        fallbackProvider = mediaProvider,
        catalogNetworkMonitor = catalogNetworkMonitor
    )

    val animeRepository: AnimeRepository = AnimeRepository(remoteMetadataProvider)

    val watchRepository: WatchRepository = WatchRepository(
        watchDao = database.watchDao(),
        watchlistDao = database.watchlistDao(),
        socialDao = database.socialDao(),
        cloudSyncManager = cloudSyncManager
    )

    val userRepository: UserRepository = UserRepository(
        adminScrapedDao = database.adminScrapedDao(),
        cloudSyncManager = cloudSyncManager
    )

    val adminRepository: AdminRepository = AdminRepository(
        mediaProvider = mediaProvider,
        adminScrapedDao = database.adminScrapedDao()
    )

    val downloadsRepository: DownloadsRepository = DownloadsRepository(
        downloadsDao = database.downloadsDao(),
        cloudSyncManager = cloudSyncManager
    )

    val watchPartyRepository: WatchPartyRepository = WatchPartyRepository(
        animeRepository = animeRepository
    )

    val malSyncRepository: MalSyncRepository = MalSyncRepository(
        malSyncDao = database.malSyncDao()
    )

    val tierListRepository: TierListRepository = TierListRepository(
        animeRepository = animeRepository
    )

    val commentsRepository: CommentsRepository = CommentsRepository(
        commentsDao = database.commentsDao()
    )

    val scheduleRepository: ScheduleRepository = ScheduleRepository(
        animeRepository = animeRepository
    )

    val quotesRepository: QuotesRepository = QuotesRepository()

    val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository(
        cloudSyncManager = cloudSyncManager
    )

    val appUpdateRepository: AppUpdateRepository = AppUpdateRepository(
        context = context,
        mediaProvider = mediaProvider,
        adminRepository = adminRepository
    )

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
