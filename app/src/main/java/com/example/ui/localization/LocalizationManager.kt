package com.example.ui.localization

import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা"),
    HINDI("hi", "Hindi", "हिन्दी"),
    ARABIC("ar", "Arabic", "العربية"),
    JAPANESE("ja", "Japanese", "日本語");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) || it.displayName.equals(code, ignoreCase = true) || it.nativeName == code }
                ?: ENGLISH
        }
    }
}

data class LocalizedStrings(
    val navHome: String,
    val navBrowse: String,
    val navSearch: String,
    val navWatchlist: String,
    val navProfile: String,
    val welcomeBack: String,
    val dayStreak: String,
    val continueWatching: String,
    val aiPicksForYou: String,
    val becauseYouWatched: String,
    val yourNextAnime: String,
    val recommendedForYou: String,
    val similarAnime: String,
    val hiddenGems: String,
    val trendingForYou: String,
    val trendingNow: String,
    val popularThisWeek: String,
    val topRated: String,
    val recentlyAdded: String,
    val airingToday: String,
    val latestEpisodes: String,
    val dailyChallenge: String,
    val personalStatistics: String,
    val yourFavorites: String,
    val yourWatchQueue: String,
    val skipIntro: String,
    val skipOutro: String,
    val syncSynced: String,
    val syncSyncing: String,
    val syncOffline: String,
    val syncError: String,
    val languageLabel: String
)

object LocalizationManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _strings = MutableStateFlow(getStringsFor(AppLanguage.ENGLISH))
    val strings: StateFlow<LocalizedStrings> = _strings.asStateFlow()

    fun setLanguage(language: AppLanguage, context: Context? = null) {
        _currentLanguage.value = language
        _strings.value = getStringsFor(language)
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        context?.let { ctx ->
            val config = Configuration(ctx.resources.configuration)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
        }
    }

    fun setLanguageByCode(codeOrName: String, context: Context? = null) {
        setLanguage(AppLanguage.fromCode(codeOrName), context)
    }

    fun getStringsFor(lang: AppLanguage): LocalizedStrings {
        return when (lang) {
            AppLanguage.ENGLISH -> LocalizedStrings(
                navHome = "Home",
                navBrowse = "Hub",
                navSearch = "Search",
                navWatchlist = "Watchlist",
                navProfile = "Profile",
                welcomeBack = "Welcome back",
                dayStreak = "Day Streak",
                continueWatching = "Continue Watching",
                aiPicksForYou = "AI Picks For You",
                becauseYouWatched = "Because You Watched",
                yourNextAnime = "Your Next Anime",
                recommendedForYou = "Recommended For You",
                similarAnime = "Similar Anime",
                hiddenGems = "Hidden Gems",
                trendingForYou = "Trending For You",
                trendingNow = "Trending Now",
                popularThisWeek = "Popular This Week",
                topRated = "Top Rated",
                recentlyAdded = "Recently Added",
                airingToday = "Airing Today",
                latestEpisodes = "Latest Episodes",
                dailyChallenge = "Today's Challenge",
                personalStatistics = "Your Watch Stats",
                yourFavorites = "Your Favorites",
                yourWatchQueue = "Your Watch Queue",
                skipIntro = "SKIP INTRO ⏩",
                skipOutro = "SKIP OUTRO ⏩",
                syncSynced = "SYNCED",
                syncSyncing = "SYNCING",
                syncOffline = "OFFLINE",
                syncError = "SYNC ERROR",
                languageLabel = "Language"
            )
            AppLanguage.BANGLA -> LocalizedStrings(
                navHome = "হোম",
                navBrowse = "হাব",
                navSearch = "সার্চ",
                navWatchlist = "ওয়াচলিস্ট",
                navProfile = "প্রোফাইল",
                welcomeBack = "স্বাগতম",
                dayStreak = "দিনের স্ট্রিক",
                continueWatching = "দেখা চালিয়ে যান",
                aiPicksForYou = "আপনার জন্য AI পছন্দ",
                becauseYouWatched = "আপনি দেখেছেন বলে",
                yourNextAnime = "আপনার পরবর্তী অ্যানিমে",
                recommendedForYou = "আপনার জন্য সুপারিশকৃত",
                similarAnime = "একই ধরনের অ্যানিমে",
                hiddenGems = "লুকানো রত্ন",
                trendingForYou = "আপনার জন্য ট্রেন্ডিং",
                trendingNow = "এখন ট্রেন্ডিং",
                popularThisWeek = "এই সপ্তাহের জনপ্রিয়",
                topRated = "সেরা রেটিংপ্রাপ্ত",
                recentlyAdded = "সম্প্রতি যুক্ত",
                airingToday = "আজ সম্প্রচারিত",
                latestEpisodes = "নতুন পর্বসমূহ",
                dailyChallenge = "আজকের চ্যালেঞ্জ",
                personalStatistics = "আপনার ওয়াচ স্ট্যাটস",
                yourFavorites = "আপনার ফেভারিট",
                yourWatchQueue = "আপনার ওয়াচ কিউ",
                skipIntro = "ইন্ট্রো স্কিপ ⏩",
                skipOutro = "আউট্রো স্কিপ ⏩",
                syncSynced = "SYNCED",
                syncSyncing = "SYNCING",
                syncOffline = "OFFLINE",
                syncError = "SYNC ERROR",
                languageLabel = "ভাষা"
            )
            AppLanguage.HINDI -> LocalizedStrings(
                navHome = "होम",
                navBrowse = "हब",
                navSearch = "खोजें",
                navWatchlist = "वॉचलिस्ट",
                navProfile = "प्रोफ़ाइल",
                welcomeBack = "वापसी पर स्वागत है",
                dayStreak = "दिन की स्ट्रीक",
                continueWatching = "देखना जारी रखें",
                aiPicksForYou = "आपके लिए AI पसंद",
                becauseYouWatched = "क्योंकि आपने देखा",
                yourNextAnime = "आपका अगला एनीमे",
                recommendedForYou = "आपके लिए अनुशंसित",
                similarAnime = "समान एनीमे",
                hiddenGems = "छुपे हुए रत्न",
                trendingForYou = "आपके लिए ट्रेंडिंग",
                trendingNow = "अभी ट्रेंडिंग",
                popularThisWeek = "इस सप्ताह लोकप्रिय",
                topRated = "टॉप रेटेड",
                recentlyAdded = "हाल ही में जोड़ा गया",
                airingToday = "आज प्रसारित",
                latestEpisodes = "नवीनतम एपिसोड",
                dailyChallenge = "आज की चुनौती",
                personalStatistics = "आपके आँकड़े",
                yourFavorites = "आपके पसंदीदा",
                yourWatchQueue = "आपकी वॉच कतार",
                skipIntro = "SKIP INTRO ⏩",
                skipOutro = "SKIP OUTRO ⏩",
                syncSynced = "SYNCED",
                syncSyncing = "SYNCING",
                syncOffline = "OFFLINE",
                syncError = "SYNC ERROR",
                languageLabel = "भाषा"
            )
            AppLanguage.ARABIC -> LocalizedStrings(
                navHome = "الرئيسية",
                navBrowse = "المركز",
                navSearch = "بحث",
                navWatchlist = "قائمتي",
                navProfile = "الملف الشخصي",
                welcomeBack = "مرحباً بعودتك",
                dayStreak = "أيام متتالية",
                continueWatching = "متابعة المشاهدة",
                aiPicksForYou = "اختيارات الذكاء الاصطناعي لك",
                becauseYouWatched = "لأنك شاهدت",
                yourNextAnime = "أنميك القادم",
                recommendedForYou = "موصى به لك",
                similarAnime = "أنمي مشابه",
                hiddenGems = "جواهر مخفية",
                trendingForYou = "رائج لك",
                trendingNow = "الرائج الآن",
                popularThisWeek = "الأكثر شعبية هذا الأسبوع",
                topRated = "الأعلى تقييماً",
                recentlyAdded = "أضيف مؤخراً",
                airingToday = "يعرض اليوم",
                latestEpisodes = "أحدث الحلقات",
                dailyChallenge = "تحدي اليوم",
                personalStatistics = "إحصائياتك",
                yourFavorites = "مفضلاتك",
                yourWatchQueue = "قائمة الانتظار",
                skipIntro = "SKIP INTRO ⏩",
                skipOutro = "SKIP OUTRO ⏩",
                syncSynced = "SYNCED",
                syncSyncing = "SYNCING",
                syncOffline = "OFFLINE",
                syncError = "SYNC ERROR",
                languageLabel = "اللغة"
            )
            AppLanguage.JAPANESE -> LocalizedStrings(
                navHome = "ホーム",
                navBrowse = "ハブ",
                navSearch = "検索",
                navWatchlist = "マイリスト",
                navProfile = "プロフィール",
                welcomeBack = "おかえりなさい",
                dayStreak = "日連続ストリーク",
                continueWatching = "視聴を続ける",
                aiPicksForYou = "AIおすすめ",
                becauseYouWatched = "視聴履歴からの推薦",
                yourNextAnime = "次に見るべきアニメ",
                recommendedForYou = "あなたへのおすすめ",
                similarAnime = "類似アニメ",
                hiddenGems = "隠れた名作",
                trendingForYou = "あなた向けのトレンド",
                trendingNow = "トレンド",
                popularThisWeek = "今週の人気アニメ",
                topRated = "高評価アニメ",
                recentlyAdded = "新着アニメ",
                airingToday = "本日放送",
                latestEpisodes = "最新エピソード",
                dailyChallenge = "今日のチャレンジ",
                personalStatistics = "視聴統計",
                yourFavorites = "お気に入り",
                yourWatchQueue = "視聴キュー",
                skipIntro = "SKIP INTRO ⏩",
                skipOutro = "SKIP OUTRO ⏩",
                syncSynced = "SYNCED",
                syncSyncing = "SYNCING",
                syncOffline = "OFFLINE",
                syncError = "SYNC ERROR",
                languageLabel = "言語"
            )
        }
    }
}
