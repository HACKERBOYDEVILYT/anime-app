package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browse : Screen("browse")
    object Search : Screen("search")
    object Watchlist : Screen("watchlist")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
    object Schedule : Screen("schedule")
    object Downloads : Screen("downloads")
    object Quiz : Screen("quiz")
    object MalSync : Screen("mal_sync")
    object WatchParty : Screen("watch_party")
    object TierList : Screen("tier_list")
    object Quotes : Screen("quotes")
    object WebPortal : Screen("web_portal")

    object Details : Screen("details/{animeId}") {
        fun createRoute(animeId: String) = "details/$animeId"
    }

    object Player : Screen("player/{animeId}/{episodeNumber}") {
        fun createRoute(animeId: String, episodeNumber: Int) = "player/$animeId/$episodeNumber"
    }
}
