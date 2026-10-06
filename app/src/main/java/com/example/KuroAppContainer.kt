package com.example

import android.content.Context
import com.example.data.local.KuroDatabase
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
import com.example.data.repository.RadioRepository
import com.example.data.repository.RetrofitMetadataProvider
import com.example.data.repository.ScheduleRepository
import com.example.data.repository.TierListRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchPartyRepository
import com.example.data.repository.WatchRepository
import com.example.data.sync.CloudSyncManager

class KuroAppContainer(context: Context) {

    private val database: KuroDatabase = KuroDatabase.getDatabase(context)

    val cloudSyncManager: CloudSyncManager = CloudSyncManager()

    val mediaProvider: LocalLicensedMediaProvider = LocalLicensedMediaProvider()

    private val remoteMetadataProvider = RetrofitMetadataProvider(
        apiService = RetrofitClient.createCatalogApiService(),
        fallbackProvider = mediaProvider
    )

    val animeRepository: AnimeRepository = AnimeRepository(remoteMetadataProvider)

    val watchRepository: WatchRepository = WatchRepository(
        watchlistDao = database.watchlistDao(),
        watchHistoryDao = database.watchHistoryDao(),
        reviewDao = database.reviewDao(),
        cloudSyncManager = cloudSyncManager
    )

    val userRepository: UserRepository = UserRepository(
        userDao = database.userDao(),
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
        watchPartyDao = database.watchPartyDao()
    )

    val malSyncRepository: MalSyncRepository = MalSyncRepository(
        malSyncDao = database.malSyncDao()
    )

    val tierListRepository: TierListRepository = TierListRepository(
        tierListDao = database.tierListDao()
    )

    val commentsRepository: CommentsRepository = CommentsRepository(
        commentsDao = database.commentsDao()
    )

    val scheduleRepository: ScheduleRepository = ScheduleRepository(
        animeRepository = animeRepository,
        watchRepository = watchRepository
    )

    val quotesRepository: QuotesRepository = QuotesRepository()

    val radioRepository: RadioRepository = RadioRepository()

    val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository(
        cloudSyncManager = cloudSyncManager
    )

    val appUpdateRepository: AppUpdateRepository = AppUpdateRepository(context)
}
