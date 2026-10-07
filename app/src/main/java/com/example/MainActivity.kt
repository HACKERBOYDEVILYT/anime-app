package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.security.AdminSecurityManager
import com.example.ui.components.AppUpdatePromptDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.SecretAdminDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.browse.BrowseScreen
import com.example.ui.screens.details.AnimeDetailsScreen
import com.example.ui.screens.downloads.DownloadsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.mal.MalSyncScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.party.WatchPartyScreen
import com.example.ui.screens.player.VideoPlayerScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.quiz.AnimeQuizScreen
import com.example.ui.screens.quotes.AnimeQuotesScreen
import com.example.ui.screens.radio.AnimeRadioScreen
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.tier.TierListScreen
import com.example.ui.screens.watchlist.WatchlistScreen
import com.example.ui.screens.web.WebPortalScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.KuroStreamTheme
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.DetailsViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.PlayerViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.SearchViewModel
import com.example.viewmodel.WatchlistViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { enableEdgeToEdge() }
        val container = KuroAppContainer.getInstance(this)

        setContent {
            KuroStreamTheme {
                KuroStreamApp(container)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuroStreamApp(container: KuroAppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val updateState by container.appUpdateRepository.updateState.collectAsStateWithLifecycle()

    val isPlayerScreen = currentRoute.startsWith("player")

    // Automatic In-App Repository Update Prompt Dialog when repository has a new update
    if (updateState.showUpdateDialog) {
        AppUpdatePromptDialog(
            updateState = updateState,
            onUpdateNowClick = {
                container.appUpdateRepository.markUpdateInstalled()
            },
            onOpenWebPortalClick = {
                container.appUpdateRepository.dismissUpdateDialog()
                navController.navigate(Screen.WebPortal.route)
            },
            onDismiss = {
                container.appUpdateRepository.dismissUpdateDialog()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isPlayerScreen && currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Browse.route,
                    Screen.Search.route,
                    Screen.Watchlist.route,
                    Screen.Profile.route
                )
            ) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(if (isPlayerScreen) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // Home Screen
                composable(Screen.Home.route) {
                    val homeViewModel = remember {
                        HomeViewModel(
                            animeRepository = container.animeRepository,
                            watchRepository = container.watchRepository,
                            userRepository = container.userRepository,
                            gamificationRepository = container.gamificationRepository,
                            cloudSyncManager = container.cloudSyncManager,
                            catalogNetworkMonitor = container.catalogNetworkMonitor
                        )
                    }
                    HomeScreen(
                        viewModel = homeViewModel,
                        onAnimeClick = { anime ->
                            navController.navigate(Screen.Details.createRoute(anime.id))
                        },
                        onWatchEpisodeClick = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onNotificationsClick = {
                            navController.navigate(Screen.Notifications.route)
                        },
                        onAdminClick = {
                            navController.navigate(Screen.Admin.route)
                        },
                        onGenreClick = { genre ->
                            navController.navigate(Screen.Search.route)
                        },
                        onScheduleClick = {
                            navController.navigate(Screen.Schedule.route)
                        },
                        onDownloadsClick = {
                            navController.navigate(Screen.Downloads.route)
                        },
                        onQuizClick = {
                            navController.navigate(Screen.Quiz.route)
                        },
                        onMalSyncClick = {
                            navController.navigate(Screen.MalSync.route)
                        },
                        onPartyClick = {
                            navController.navigate(Screen.WatchParty.route)
                        },
                        onRadioClick = {
                            navController.navigate(Screen.AnimeRadio.route)
                        },
                        onTierListClick = {
                            navController.navigate(Screen.TierList.route)
                        },
                        onQuotesClick = {
                            navController.navigate(Screen.Quotes.route)
                        },
                        onWebPortalClick = {
                            navController.navigate(Screen.WebPortal.route)
                        }
                    )
                }

                // Browse Screen (AI Assistant, Cloud CDN APIs, Manga Reader & Enterprise Hub)
                composable(Screen.Browse.route) {
                    val catalogSnapshot = remember { container.mediaProvider.getAllCatalogSnapshot() }
                    BrowseScreen(
                        onCategoryClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        catalog = catalogSnapshot,
                        onAnimeClick = { anime ->
                            navController.navigate(Screen.Details.createRoute(anime.id))
                        },
                        onWatchEpisodeClick = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        },
                        onAddStreamingServer = { name, url, category ->
                            container.adminRepository.addApiConfig(name, url, category, "")
                        }
                    )
                }

                // Search Screen
                composable(Screen.Search.route) {
                    val searchViewModel = remember {
                        SearchViewModel(
                            animeRepository = container.animeRepository,
                            watchRepository = container.watchRepository,
                            gamificationRepository = container.gamificationRepository,
                            catalogNetworkMonitor = container.catalogNetworkMonitor
                        )
                    }
                    SearchScreen(
                        viewModel = searchViewModel,
                        onAnimeClick = { anime ->
                            navController.navigate(Screen.Details.createRoute(anime.id))
                        },
                        onWatchEpisodeClick = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        }
                    )
                }

                // Watchlist Screen
                composable(Screen.Watchlist.route) {
                    val watchlistViewModel = remember {
                        WatchlistViewModel(
                            watchRepository = container.watchRepository,
                            gamificationRepository = container.gamificationRepository
                        )
                    }
                    WatchlistScreen(
                        viewModel = watchlistViewModel,
                        onAnimeIdClick = { animeId ->
                            navController.navigate(Screen.Details.createRoute(animeId))
                        },
                        onWatchEpisodeClick = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        }
                    )
                }

                // Profile Screen
                composable(Screen.Profile.route) {
                    val profileViewModel = remember {
                        ProfileViewModel(
                            userRepository = container.userRepository,
                            watchRepository = container.watchRepository,
                            gamificationRepository = container.gamificationRepository,
                            cloudSyncManager = container.cloudSyncManager,
                            downloadsRepository = container.downloadsRepository
                        )
                    }
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onAdminClick = {
                            navController.navigate(Screen.Admin.route)
                        },
                        onScheduleClick = {
                            navController.navigate(Screen.Schedule.route)
                        },
                        onDownloadsClick = {
                            navController.navigate(Screen.Downloads.route)
                        },
                        onQuizClick = {
                            navController.navigate(Screen.Quiz.route)
                        },
                        onMalSyncClick = {
                            navController.navigate(Screen.MalSync.route)
                        }
                    )
                }

                // Airing Schedule Screen
                composable(Screen.Schedule.route) {
                    ScheduleScreen(
                        scheduleRepository = container.scheduleRepository,
                        onBack = { navController.popBackStack() },
                        onAnimeClick = { animeId ->
                            navController.navigate(Screen.Details.createRoute(animeId))
                        },
                        onPlayClick = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        }
                    )
                }

                // Downloads Screen
                composable(Screen.Downloads.route) {
                    DownloadsScreen(
                        downloadsRepository = container.downloadsRepository,
                        onBack = { navController.popBackStack() },
                        onPlayOfflineEpisode = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        }
                    )
                }

                // Find My Anime Quiz Screen
                composable(Screen.Quiz.route) {
                    AnimeQuizScreen(
                        animeRepository = container.animeRepository,
                        onBack = { navController.popBackStack() },
                        onAnimeClick = { animeId ->
                            navController.navigate(Screen.Details.createRoute(animeId))
                        },
                        onWatchEpisode = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        }
                    )
                }

                // MAL & AniList Sync Screen
                composable(Screen.MalSync.route) {
                    MalSyncScreen(
                        malSyncRepository = container.malSyncRepository,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Unified Live Website Portal & Repository Auto-Update Screen
                composable(Screen.WebPortal.route) {
                    WebPortalScreen(
                        appUpdateRepository = container.appUpdateRepository,
                        onWatchEpisode = { animeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(animeId, epNum))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Watch Party Screen
                composable(Screen.WatchParty.route) {
                    WatchPartyScreen(
                        watchPartyRepository = container.watchPartyRepository,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 24/7 Anime Radio Screen
                composable(Screen.AnimeRadio.route) {
                    AnimeRadioScreen(
                        radioRepository = container.radioRepository,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Anime Tier List Screen
                composable(Screen.TierList.route) {
                    TierListScreen(
                        tierListRepository = container.tierListRepository,
                        onBack = { navController.popBackStack() },
                        onAnimeClick = { animeId ->
                            navController.navigate(Screen.Details.createRoute(animeId))
                        }
                    )
                }

                // Anime Quotes Screen
                composable(Screen.Quotes.route) {
                    AnimeQuotesScreen(
                        quotesRepository = container.quotesRepository,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Notifications Screen
                composable(Screen.Notifications.route) {
                    val notifications by container.watchRepository.getNotifications().collectAsStateWithLifecycle(emptyList())
                    NotificationsScreen(
                        notifications = notifications,
                        watchRepository = container.watchRepository,
                        onBack = { navController.popBackStack() },
                        onAnimeClick = { animeId ->
                            navController.navigate(Screen.Details.createRoute(animeId))
                        }
                    )
                }

                // Anime Details Screen
                composable(
                    route = Screen.Details.route,
                    arguments = listOf(navArgument("animeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                    val detailsViewModel = remember(animeId) {
                        DetailsViewModel(animeId, container.animeRepository, container.watchRepository)
                    }
                    AnimeDetailsScreen(
                        viewModel = detailsViewModel,
                        downloadsRepository = container.downloadsRepository,
                        onBack = { navController.popBackStack() },
                        onPlayEpisode = { targetAnimeId, epNum ->
                            navController.navigate(Screen.Player.createRoute(targetAnimeId, epNum))
                        },
                        onAnimeClick = { nextAnime ->
                            navController.navigate(Screen.Details.createRoute(nextAnime.id))
                        }
                    )
                }

                // Video Player Screen
                composable(
                    route = Screen.Player.route,
                    arguments = listOf(
                        navArgument("animeId") { type = NavType.StringType },
                        navArgument("episodeNumber") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                    val episodeNum = backStackEntry.arguments?.getInt("episodeNumber") ?: 1
                    val playerViewModel = remember(animeId, episodeNum) {
                        PlayerViewModel(
                            animeId = animeId,
                            initialEpisodeNumber = episodeNum,
                            animeRepository = container.animeRepository,
                            watchRepository = container.watchRepository,
                            userRepository = container.userRepository
                        )
                    }
                    VideoPlayerScreen(
                        viewModel = playerViewModel,
                        commentsRepository = container.commentsRepository,
                        downloadsRepository = container.downloadsRepository,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Admin Dashboard Screen (Protected with security gate & session timeout)
                composable(Screen.Admin.route) {
                    val isAuthenticated by AdminSecurityManager.isAdminAuthenticated.collectAsStateWithLifecycle()

                    if (!isAuthenticated) {
                        SecretAdminDialog(
                            onDismiss = { navController.popBackStack() },
                            onSuccess = { /* Automatically refreshes state */ }
                        )
                    } else {
                        val adminViewModel = remember {
                            AdminViewModel(container.adminRepository, container.animeRepository)
                        }
                        AdminDashboardScreen(
                            viewModel = adminViewModel,
                            onBack = {
                                AdminSecurityManager.logout()
                                navController.popBackStack()
                            },
                            onPlayStream = { animeId, epNum ->
                                navController.navigate(Screen.Player.createRoute(animeId, epNum))
                            }
                        )
                    }
                }
            }
        }
    }
}
