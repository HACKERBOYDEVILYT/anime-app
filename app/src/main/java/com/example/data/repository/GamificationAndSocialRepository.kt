package com.example.data.repository

import com.example.data.model.AchievementBadge
import com.example.data.model.Anime
import com.example.data.model.AnimeBattle
import com.example.data.model.AnimeClipItem
import com.example.data.model.AnimeNewsItem
import com.example.data.model.ChallengeQuest
import com.example.data.model.CharacterProfile
import com.example.data.model.CommunityPoll
import com.example.data.model.EpisodeNote
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MonthlyMetric
import com.example.data.model.PollOption
import com.example.data.model.SeasonalEvent
import com.example.data.model.SocialActivityItem
import com.example.data.model.SocialUserProfile
import com.example.data.model.StudioStaffProfile
import com.example.data.model.TrendingHeatItem
import com.example.data.model.TriviaQuestion
import com.example.data.model.VideoBookmark
import com.example.data.model.WallpaperItem
import com.example.data.model.WatchQueueItem
import com.example.data.model.WatchlistCollection
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GamificationAndSocialRepository(
    private val cloudSyncManager: CloudSyncManager? = null
) {

    // 1. 14 Badges & Achievements (Section 13)
    private val _achievements = MutableStateFlow(
        listOf(
            AchievementBadge("ach_first_anime", "First Anime", "Add or start your very first anime series", "🎬", 50, true, 1, 1, "WATCH"),
            AchievementBadge("ach_first_ep", "First Episode", "Complete watching your first anime episode", "▶️", 25, true, 1, 1, "WATCH"),
            AchievementBadge("ach_first_review", "First Review", "Write and publish your first anime review", "✍️", 50, true, 1, 1, "SOCIAL"),
            AchievementBadge("ach_first_completed", "First Completed Anime", "Finish all episodes of an anime series", "🏁", 100, true, 1, 1, "WATCH"),
            AchievementBadge("ach_100_ep", "100 Episodes", "Watch 100 total anime episodes", "🔥", 150, true, 142, 100, "WATCH"),
            AchievementBadge("ach_500_ep", "500 Episodes", "Watch 500 total anime episodes", "⚡", 300, false, 142, 500, "WATCH"),
            AchievementBadge("ach_1000_ep", "1000 Episodes", "Watch 1,000 total anime episodes", "👑", 750, false, 142, 1000, "WATCH"),
            AchievementBadge("ach_shounen", "Shounen Master", "Complete 10 Shounen action anime series", "⚔️", 200, true, 10, 10, "GENRE"),
            AchievementBadge("ach_romance", "Romance Expert", "Complete 5 Romance anime series", "💖", 150, false, 3, 5, "GENRE"),
            AchievementBadge("ach_comedy", "Comedy Expert", "Complete 5 Comedy anime series", "😂", 150, true, 5, 5, "GENRE"),
            AchievementBadge("ach_action", "Action Expert", "Watch 50 Action anime episodes", "💥", 200, true, 50, 50, "GENRE"),
            AchievementBadge("ach_night_owl", "Night Owl", "Stream anime past midnight on 5 nights", "🦉", 120, true, 5, 5, "STREAK"),
            AchievementBadge("ach_weekend", "Weekend Warrior", "Binge 6+ episodes over a single weekend", "🛡️", 150, true, 6, 6, "STREAK"),
            AchievementBadge("ach_master", "Anime Master", "Reach Level 100 & unlock elite master rank", "🏆", 1000, false, 25, 100, "MASTER")
        )
    )
    val achievements: StateFlow<List<AchievementBadge>> = _achievements.asStateFlow()

    // 2. Daily, Weekly & Monthly Challenges (Section 14)
    private val _challenges = MutableStateFlow(
        listOf(
            ChallengeQuest("q_daily_1", "Watch 1 Episode", "Stream any episode today to maintain your streak", "DAILY", 1, 1, 50, "Daily Streaker", isCompleted = true, isClaimed = false),
            ChallengeQuest("q_daily_2", "Rate 1 Anime", "Give a star rating to any anime in the catalog", "DAILY", 0, 1, 35, null, isCompleted = false, isClaimed = false),
            ChallengeQuest("q_weekly_1", "Watch 5 Episodes", "Stream 5 episodes across this week", "WEEKLY", 4, 5, 200, "Weekend Warrior", isCompleted = false, isClaimed = false),
            ChallengeQuest("q_weekly_2", "Complete 1 Anime", "Finish an entire anime season this week", "WEEKLY", 1, 1, 200, "Season Finisher", isCompleted = true, isClaimed = false),
            ChallengeQuest("q_weekly_3", "Write 1 Review", "Share your review with the community", "WEEKLY", 0, 1, 100, "Critic Badge", isCompleted = false, isClaimed = false),
            ChallengeQuest("q_monthly_1", "Watch 10 Episodes", "Complete 10 episodes this month", "MONTHLY", 8, 10, 400, "Otaku Elite", isCompleted = false, isClaimed = false),
            ChallengeQuest("q_monthly_2", "Complete 3 Anime", "Finish 3 full anime series this month", "MONTHLY", 2, 3, 600, "Anime Master Crest", isCompleted = false, isClaimed = false)
        )
    )
    val challenges: StateFlow<List<ChallengeQuest>> = _challenges.asStateFlow()

    // 3. Seasonal Events (Halloween, New Year, Summer)
    private val _seasonalEvents = MutableStateFlow(
        listOf(
            SeasonalEvent(
                id = "evt_halloween",
                title = "Halloween Supernatural Anime Event 🎃",
                subtitle = "Watch dark fantasy & supernatural anime for 2x XP & Cursed Spirit Crest",
                themeColorHex = "#FF6D00",
                bannerEmoji = "🎃",
                activePeriod = "Oct 1 – Nov 2",
                xpMultiplier = 2.0f,
                specialBadgeTitle = "Halloween Soul Reaper",
                currentPoints = 420,
                targetPoints = 500,
                isJoined = true
            ),
            SeasonalEvent(
                id = "evt_new_year",
                title = "New Year Winter Simulcast Festival 🎆",
                subtitle = "Celebrate the Winter premiere lineup with bonus XP rewards",
                themeColorHex = "#00E5FF",
                bannerEmoji = "🎆",
                activePeriod = "Dec 28 – Jan 15",
                xpMultiplier = 1.5f,
                specialBadgeTitle = "New Year Hokage",
                currentPoints = 280,
                targetPoints = 500,
                isJoined = true
            ),
            SeasonalEvent(
                id = "evt_summer",
                title = "Summer Anime Matsuri Marathon ☀️",
                subtitle = "Complete summer festival episodes & unlock the Matsuri Champion badge",
                themeColorHex = "#FF2A5F",
                bannerEmoji = "☀️",
                activePeriod = "Jul 1 – Aug 31",
                xpMultiplier = 1.75f,
                specialBadgeTitle = "Summer Matsuri Champion",
                currentPoints = 500,
                targetPoints = 500,
                isJoined = true
            )
        )
    )
    val seasonalEvents: StateFlow<List<SeasonalEvent>> = _seasonalEvents.asStateFlow()

    // 4. Personal Watch Statistics Charts (Section 15)
    val monthlyMetrics: List<MonthlyMetric> = listOf(
        MonthlyMetric("May", 8.5f, 21),
        MonthlyMetric("Jun", 11.2f, 28),
        MonthlyMetric("Jul", 14.0f, 35),
        MonthlyMetric("Aug", 9.6f, 24),
        MonthlyMetric("Sep", 13.5f, 34),
        MonthlyMetric("Oct", 16.4f, 41)
    )

    val genreDistribution: Map<String, Float> = mapOf(
        "Action" to 34f,
        "Fantasy" to 26f,
        "Sci-Fi" to 16f,
        "Romance" to 14f,
        "Comedy" to 10f
    )

    val studioDistribution: Map<String, Float> = mapOf(
        "MAPPA" to 32f,
        "Madhouse" to 27f,
        "ufotable" to 23f,
        "A-1 Pictures" to 18f
    )

    // 5. Social System (Followers, Following, Activity Feed) (Section 11)
    private val _socialUsers = MutableStateFlow(
        listOf(
            SocialUserProfile("u_soc_1", "AkiraVortex", "https://api.dicebear.com/7.x/bottts/png?seed=AkiraVortex", "Elite Otaku", 54, 6210, 310, 28, "Jujutsu Kaisen", isFollowing = true, isFollower = true),
            SocialUserProfile("u_soc_2", "HinataSakura", "https://api.dicebear.com/7.x/bottts/png?seed=HinataSakura", "Otaku", 32, 3680, 185, 19, "Frieren: Beyond Journey's End", isFollowing = true, isFollower = true),
            SocialUserProfile("u_soc_3", "ZoroSwordsman", "https://api.dicebear.com/7.x/bottts/png?seed=ZoroSwordsman", "Anime Master", 100, 12400, 1150, 64, "One Piece", isFollowing = false, isFollower = true),
            SocialUserProfile("u_soc_4", "GojoInfinity", "https://api.dicebear.com/7.x/bottts/png?seed=GojoInfinity", "Elite Otaku", 61, 7015, 420, 35, "Solo Leveling", isFollowing = true, isFollower = false),
            SocialUserProfile("u_soc_5", "NezukoChan", "https://api.dicebear.com/7.x/bottts/png?seed=NezukoChan", "Anime Fan", 18, 2070, 95, 11, "Demon Slayer", isFollowing = false, isFollower = true)
        )
    )
    val socialUsers: StateFlow<List<SocialUserProfile>> = _socialUsers.asStateFlow()

    private val _activityFeed = MutableStateFlow(
        listOf(
            SocialActivityItem("act_1", "u_soc_1", "AkiraVortex", "https://api.dicebear.com/7.x/bottts/png?seed=AkiraVortex", "completed Naruto", "anime_2", "Naruto", null, "4m ago", 24, false),
            SocialActivityItem("act_2", "u_soc_3", "ZoroSwordsman", "https://api.dicebear.com/7.x/bottts/png?seed=ZoroSwordsman", "rated One Piece 5 stars", "anime_1", "One Piece", 5, "15m ago", 42, true),
            SocialActivityItem("act_3", "u_soc_2", "HinataSakura", "https://api.dicebear.com/7.x/bottts/png?seed=HinataSakura", "added Bleach to favorites", "anime_3", "Bleach", null, "32m ago", 19, false),
            SocialActivityItem("act_4", "u_soc_4", "GojoInfinity", "https://api.dicebear.com/7.x/bottts/png?seed=GojoInfinity", "completed Frieren: Beyond Journey's End", "anime_1", "Frieren: Beyond Journey's End", 5, "1h ago", 37, false)
        )
    )
    val activityFeed: StateFlow<List<SocialActivityItem>> = _activityFeed.asStateFlow()

    // 6. Global Leaderboards (Top Watchers, Top Reviewers, Top Contributors, Weekly XP, Monthly XP)
    val leaderboards: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(1, "u_soc_3", "ZoroSwordsman", "https://api.dicebear.com/7.x/bottts/png?seed=ZoroSwordsman", "Anime Master", 100, "1,150 Episodes • 12,400 XP", "TOP_WATCHERS"),
        LeaderboardEntry(2, "u_soc_4", "GojoInfinity", "https://api.dicebear.com/7.x/bottts/png?seed=GojoInfinity", "Elite Otaku", 61, "420 Episodes • 7,015 XP", "TOP_WATCHERS"),
        LeaderboardEntry(3, "u_soc_1", "AkiraVortex", "https://api.dicebear.com/7.x/bottts/png?seed=AkiraVortex", "Elite Otaku", 54, "310 Episodes • 6,210 XP", "TOP_WATCHERS"),
        LeaderboardEntry(4, "u_default_01", "Robiul", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80", "Otaku", 25, "142 Episodes • 2,850 XP", "TOP_WATCHERS"),
        LeaderboardEntry(1, "u_soc_3", "ZoroSwordsman", "https://api.dicebear.com/7.x/bottts/png?seed=ZoroSwordsman", "Anime Master", 100, "64 Reviews • 1,890 Likes", "TOP_REVIEWERS"),
        LeaderboardEntry(2, "u_soc_4", "GojoInfinity", "https://api.dicebear.com/7.x/bottts/png?seed=GojoInfinity", "Elite Otaku", 61, "35 Reviews • 940 Likes", "TOP_REVIEWERS"),
        LeaderboardEntry(1, "u_default_01", "Robiul", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80", "Otaku", 25, "17 Servers + 48 Subtitles", "TOP_CONTRIBUTORS"),
        LeaderboardEntry(1, "u_soc_1", "AkiraVortex", "https://api.dicebear.com/7.x/bottts/png?seed=AkiraVortex", "Elite Otaku", 54, "+1,450 XP this week", "WEEKLY_XP"),
        LeaderboardEntry(2, "u_default_01", "Robiul", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80", "Otaku", 25, "+980 XP this week", "WEEKLY_XP"),
        LeaderboardEntry(1, "u_soc_3", "ZoroSwordsman", "https://api.dicebear.com/7.x/bottts/png?seed=ZoroSwordsman", "Anime Master", 100, "+4,800 XP this month", "MONTHLY_XP")
    )

    // 7. Character Database (Profiles, VA, Relationships, Favorite toggle, Search)
    private val _characters = MutableStateFlow(
        listOf(
            CharacterProfile(
                id = "char_frieren",
                name = "Frieren",
                japaneseName = "フリーレン",
                animeId = "anime_1",
                animeTitle = "Frieren: Beyond Journey's End",
                role = "Main Protagonist",
                avatarUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
                voiceActor = "Atsumi Tanezaki (JP) / Mallorie Rodak (EN)",
                bio = "Elven mage of the Hero Party who embarks on a new journey to understand humanity after the passing of Himmel.",
                relationships = listOf("Himmel (Hero Party Comrade)", "Fern (Apprentice)", "Stark (Vanguard Companion)", "Heiter (Comrade)"),
                likesCount = 19420,
                isFavorite = true
            ),
            CharacterProfile(
                id = "char_gojo",
                name = "Satoru Gojo",
                japaneseName = "五条 悟",
                animeId = "anime_2",
                animeTitle = "Jujutsu Kaisen",
                role = "Main • Special Grade Sorcerer",
                avatarUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
                voiceActor = "Yuichi Nakamura (JP) / Kaiji Tang (EN)",
                bio = "The strongest modern Jujutsu Sorcerer, wielder of the Six Eyes and Limitless cursed technique.",
                relationships = listOf("Suguru Geto (Best Friend)", "Yuji Itadori (Student)", "Megumi Fushiguro (Student)", "Ryomen Sukuna (Rival)"),
                likesCount = 28900,
                isFavorite = true
            ),
            CharacterProfile(
                id = "char_jinwoo",
                name = "Sung Jinwoo",
                japaneseName = "성진우 / 水篠 旬",
                animeId = "anime_3",
                animeTitle = "Solo Leveling",
                role = "Main Protagonist • Shadow Monarch",
                avatarUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
                voiceActor = "Taito Ban (JP) / Aleks Le (EN)",
                bio = "Once known as humanity's weakest E-Rank hunter, awakened by the System to command the Shadow Army.",
                relationships = listOf("Cha Hae-In (Ally)", "Yoo Jinho (Guild Vice-Master)", "Igris (Shadow Knight)"),
                likesCount = 24100,
                isFavorite = true
            ),
            CharacterProfile(
                id = "char_tanjiro",
                name = "Tanjiro Kamado",
                japaneseName = "竈門 炭治郎",
                animeId = "anime_4",
                animeTitle = "Demon Slayer: Kimetsu no Yaiba",
                role = "Main Protagonist",
                avatarUrl = "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg",
                voiceActor = "Natsuki Hanae (JP) / Zach Aguilar (EN)",
                bio = "Kind-hearted Demon Slayer who masters Sun Breathing and Water Breathing to cure his sister Nezuko.",
                relationships = listOf("Nezuko Kamado (Sister)", "Zenitsu Agatsuma (Comrade)", "Inosuke Hashibira (Comrade)", "Giyu Tomioka (Mentor)"),
                likesCount = 21300,
                isFavorite = false
            ),
            CharacterProfile(
                id = "char_luffy",
                name = "Monkey D. Luffy",
                japaneseName = "モンキー・D・ルフィ",
                animeId = "anime_5",
                animeTitle = "One Piece",
                role = "Main Protagonist • Straw Hat Captain",
                avatarUrl = "https://cdn.myanimelist.net/images/anime/6/73245l.jpg",
                voiceActor = "Mayumi Tanaka (JP) / Colleen Clinkenbeard (EN)",
                bio = "Captain of the Straw Hat Pirates who ate the Gum-Gum (Hito Hito no Mi, Model: Nika) Devil Fruit.",
                relationships = listOf("Roronoa Zoro (First Mate)", "Shanks (Inspiration)", "Portgas D. Ace (Sworn Brother)", "Sabo (Sworn Brother)"),
                likesCount = 31500,
                isFavorite = true
            ),
            CharacterProfile(
                id = "char_naruto",
                name = "Naruto Uzumaki",
                japaneseName = "うずまき ナルト",
                animeId = "anime_6",
                animeTitle = "Naruto Shippuden",
                role = "Main Protagonist • Seventh Hokage",
                avatarUrl = "https://cdn.myanimelist.net/images/anime/13/17405l.jpg",
                voiceActor = "Junko Takeuchi (JP) / Maile Flanagan (EN)",
                bio = "Shinobi of Konohagakure and Jinchuriki of Kurama who achieved his dream of becoming Hokage.",
                relationships = listOf("Sasuke Uchiha (Rival & Brother)", "Kakashi Hatake (Sensei)", "Jiraiya (Master)", "Hinata Hyuga (Wife)"),
                likesCount = 29800,
                isFavorite = false
            )
        )
    )
    val characters: StateFlow<List<CharacterProfile>> = _characters.asStateFlow()

    // 8. Studio & Staff Pages
    val studiosAndStaff: List<StudioStaffProfile> = listOf(
        StudioStaffProfile(
            id = "studio_mappa",
            studioName = "MAPPA",
            foundedYear = 2011,
            headquarters = "Suginami, Tokyo, Japan",
            director = "Shota Goshozono / Yuichiro Hayashi",
            headWriter = "Hiroshi Seko",
            leadAnimator = "imashi / Tadashi Hiramatsu",
            featuredVoiceActors = listOf("Yuichi Nakamura", "Junya Enoki", "Megumi Ogata", "Kenjiro Tsuda"),
            notableWorks = listOf("Jujutsu Kaisen", "Attack on Titan: The Final Season", "Chainsaw Man", "Vinland Saga Season 2"),
            averageScore = 9.3f,
            logoUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg"
        ),
        StudioStaffProfile(
            id = "studio_madhouse",
            studioName = "Madhouse",
            foundedYear = 1972,
            headquarters = "Nakano, Tokyo, Japan",
            director = "Keiichiro Saito",
            headWriter = "Tomohiro Suzuki",
            leadAnimator = "Reiko Nagasawa / Keisuke Kojima",
            featuredVoiceActors = listOf("Atsumi Tanezaki", "Kana Ichinose", "Nobuhiko Okamoto", "Mamoru Miyano"),
            notableWorks = listOf("Frieren: Beyond Journey's End", "Hunter x Hunter (2011)", "Death Note", "One Punch Man S1"),
            averageScore = 9.5f,
            logoUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg"
        ),
        StudioStaffProfile(
            id = "studio_ufotable",
            studioName = "ufotable",
            foundedYear = 2000,
            headquarters = "Nakano, Tokyo, Japan",
            director = "Haruo Sotozaki",
            headWriter = "ufotable Script Collective",
            leadAnimator = "Akira Matsushima / Nozomu Abe",
            featuredVoiceActors = listOf("Natsuki Hanae", "Akari Kito", "Hiro Shimono", "Yoshitsugu Matsuoka"),
            notableWorks = listOf("Demon Slayer: Kimetsu no Yaiba", "Fate/stay night: Unlimited Blade Works", "Fate/Zero"),
            averageScore = 9.2f,
            logoUrl = "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg"
        ),
        StudioStaffProfile(
            id = "studio_a1",
            studioName = "A-1 Pictures",
            foundedYear = 2005,
            headquarters = "Suginami, Tokyo, Japan",
            director = "Shunsuke Nakashige",
            headWriter = "Noboru Kimura",
            leadAnimator = "Tomoko Sudo / Yoshihiro Kanno",
            featuredVoiceActors = listOf("Taito Ban", "Reina Ueda", "Genta Nakamura", "Haruka Tomatsu"),
            notableWorks = listOf("Solo Leveling", "Kaguya-sama: Love is War", "86 Eighty-Six", "Sword Art Online"),
            averageScore = 9.0f,
            logoUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg"
        )
    )

    // 9. Anime Trailers & Clips
    val animeClips: List<AnimeClipItem> = listOf(
        AnimeClipItem("clip_1", "anime_1", "Frieren: Beyond Journey's End", "Official Main Trailer (1080p)", "Trailer", "02:15", "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg", "https://v.animethemes.moe/SousouNoFrieren-OP1-NCBD1080.webm", "840K views"),
        AnimeClipItem("clip_2", "anime_2", "Jujutsu Kaisen Season 2", "Shibuya Incident Teaser PV", "Teaser", "01:45", "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg", "https://v.animethemes.moe/JujutsuKaisenS2-OP2-NCBD1080.webm", "1.2M views"),
        AnimeClipItem("clip_3", "anime_3", "Solo Leveling", "Creditless Opening Preview (LEveL)", "Opening", "01:30", "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg", "https://v.animethemes.moe/OreDakeLevelUpNaKen-OP1-NCBD1080.webm", "950K views"),
        AnimeClipItem("clip_4", "anime_4", "Demon Slayer", "Hashira Character PV • Infinity Castle", "Character PV", "02:05", "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg", "https://v.animethemes.moe/KimetsuNoYaibaHashiraGeikoHen-OP1.webm", "1.5M views"),
        AnimeClipItem("clip_5", "anime_1", "Frieren: Beyond Journey's End", "Anytime Anywhere Creditless Ending", "Ending", "01:30", "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg", "https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8", "620K views"),
        AnimeClipItem("clip_6", "anime_2", "Jujutsu Kaisen", "Anime Expo Special Production News Clip", "News Clip", "03:10", "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8", "410K views")
    )

    // 10. Anime News Center
    val animeNews: List<AnimeNewsItem> = listOf(
        AnimeNewsItem("news_1", "Frieren: Beyond Journey's End Season 2 Officially Confirmed by Madhouse", "Season Announcement", "Madhouse reveals full production staff and key visual for Frieren's Northern Plateau expedition arc.", "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg", "2 hours ago", "anime_1"),
        AnimeNewsItem("news_2", "Demon Slayer: Infinity Castle Trilogy Movie Announcement & IMAX Simulcast", "Movie Announcement", "ufotable announces global theatrical and 4K HDR streaming roadmap for the Infinity Castle finale.", "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg", "5 hours ago", "anime_4"),
        AnimeNewsItem("news_3", "Solo Leveling Season 2: Arise from the Shadow Simulcast Schedule Released", "Release News", "A-1 Pictures confirms 1080p 60fps simulcast availability across all 17 edge servers.", "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg", "1 day ago", "anime_3"),
        AnimeNewsItem("news_4", "MAPPA Opens New Next-Gen 2D/3D Sakuga Animation Department in Tokyo", "Studio News", "The studio expands its in-house key animation pipeline for Culling Game and Chainsaw Man.", "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg", "2 days ago", "anime_2"),
        AnimeNewsItem("news_5", "New Original Sci-Fi Cyberpunk Anime Announced for Next Season", "New Anime", "Acclaimed directors team up for a 24-episode high-octane cyberpunk thriller.", "https://cdn.myanimelist.net/images/anime/1818/126431l.jpg", "3 days ago", "anime_5")
    )

    // 11. Community Polls
    private val _communityPolls = MutableStateFlow(
        listOf(
            CommunityPoll(
                id = "poll_best_anime",
                category = "Best Anime • Monthly Poll",
                question = "Which anime deserves Anime of the Year?",
                options = listOf(
                    PollOption("opt_1", "Frieren: Beyond Journey's End", 1420),
                    PollOption("opt_2", "Jujutsu Kaisen Season 2", 1290),
                    PollOption("opt_3", "Solo Leveling", 1180),
                    PollOption("opt_4", "One Piece (Egghead Arc)", 1050)
                )
            ),
            CommunityPoll(
                id = "poll_best_char",
                category = "Best Character • Weekly Poll",
                question = "Who is the most iconic character this season?",
                options = listOf(
                    PollOption("opt_c1", "Satoru Gojo", 1840),
                    PollOption("opt_c2", "Frieren", 1620),
                    PollOption("opt_c3", "Sung Jinwoo", 1510),
                    PollOption("opt_c4", "Roronoa Zoro", 1390)
                )
            ),
            CommunityPoll(
                id = "poll_best_op",
                category = "Best Opening",
                question = "Which anime opening do you never skip?",
                options = listOf(
                    PollOption("opt_o1", "SPECIALZ — King Gnu (JJK S2)", 1540),
                    PollOption("opt_o2", "Yuusha — YOASOBI (Frieren)", 1490),
                    PollOption("opt_o3", "LEveL — SawanoHiroyuki[nZk] (Solo Leveling)", 1120)
                )
            ),
            CommunityPoll(
                id = "poll_best_fight",
                category = "Best Fight",
                question = "Which battle had the greatest animation sakuga?",
                options = listOf(
                    PollOption("opt_f1", "Sukuna vs Mahoraga (Shibuya)", 1920),
                    PollOption("opt_f2", "Frieren & Fern vs Clone Frieren", 1650),
                    PollOption("opt_f3", "Sung Jinwoo vs Blood-Red Commander Igris", 1580)
                )
            )
        )
    )
    val communityPolls: StateFlow<List<CommunityPoll>> = _communityPolls.asStateFlow()

    // 12. Anime Battles (Goku vs Naruto, Gojo vs Sukuna)
    private val _animeBattles = MutableStateFlow(
        listOf(
            AnimeBattle(
                id = "battle_goku_naruto",
                title = "Ultimate Shonen Icons Clash",
                leftFighterName = "Goku",
                leftFighterAnime = "Dragon Ball Super",
                leftFighterAvatar = "https://api.dicebear.com/7.x/bottts/png?seed=GokuUltra",
                leftVotes = 4820,
                rightFighterName = "Naruto",
                rightFighterAnime = "Naruto Shippuden",
                rightFighterAvatar = "https://cdn.myanimelist.net/images/anime/13/17405l.jpg",
                rightVotes = 4390
            ),
            AnimeBattle(
                id = "battle_gojo_sukuna",
                title = "Battle of the Strongest Sorcerers",
                leftFighterName = "Gojo",
                leftFighterAnime = "Jujutsu Kaisen",
                leftFighterAvatar = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
                leftVotes = 6150,
                rightFighterName = "Sukuna",
                rightFighterAnime = "Jujutsu Kaisen",
                rightFighterAvatar = "https://api.dicebear.com/7.x/bottts/png?seed=SukunaKing",
                rightVotes = 5420
            ),
            AnimeBattle(
                id = "battle_frieren_jinwoo",
                title = "Mage of a Thousand Years vs Shadow Monarch",
                leftFighterName = "Frieren",
                leftFighterAnime = "Frieren: Beyond Journey's End",
                leftFighterAvatar = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
                leftVotes = 3980,
                rightFighterName = "Sung Jinwoo",
                rightFighterAnime = "Solo Leveling",
                rightFighterAvatar = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
                rightVotes = 4120
            )
        )
    )
    val animeBattles: StateFlow<List<AnimeBattle>> = _animeBattles.asStateFlow()

    // 13. Wallpaper Center (Lock Screen, Home Screen, Character Wallpapers)
    private val _wallpapers = MutableStateFlow(
        listOf(
            WallpaperItem("wp_1", "Frieren Starry Grimoire Sky", "Frieren: Beyond Journey's End", "Lock Screen", "4K AMOLED (2160x3840)", "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg", 3420, true),
            WallpaperItem("wp_2", "Satoru Gojo • Unlimited Void", "Jujutsu Kaisen", "Character Wallpaper", "4K UHD (2160x3840)", "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg", 5180, true),
            WallpaperItem("wp_3", "Sung Jinwoo • Arise Shadow Army", "Solo Leveling", "Home Screen", "4K AMOLED (2160x3840)", "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg", 4690, false),
            WallpaperItem("wp_4", "Tanjiro • Hinokami Kagura", "Demon Slayer", "Lock Screen", "4K UHD (2160x3840)", "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg", 3890, false),
            WallpaperItem("wp_5", "Gear 5 Luffy • Sun God Nika", "One Piece", "Character Wallpaper", "4K UHD (2160x3840)", "https://cdn.myanimelist.net/images/anime/6/73245l.jpg", 6120, true),
            WallpaperItem("wp_6", "Cyberpunk Night City Neon Skyline", "Cyberpunk: Edgerunners", "Home Screen", "4K Ultrawide", "https://cdn.myanimelist.net/images/anime/1818/126431l.jpg", 2940, false)
        )
    )
    val wallpapers: StateFlow<List<WallpaperItem>> = _wallpapers.asStateFlow()

    // 14. Anime Trivia & Mini Games (Guess the Character, Guess the Anime, Opening Quiz, Emoji Anime Quiz)
    val triviaAndMiniGames: List<TriviaQuestion> = listOf(
        TriviaQuestion(
            id = "triv_1",
            category = "Guess the Character",
            promptText = "Who is this character? Known for wielding the Six Eyes and Limitless technique.",
            hintOrEmoji = "🤞🔵🔴♾️",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
            options = listOf("Satoru Gojo", "Kakashi Hatake", "Killua Zoldyck", "Levi Ackerman"),
            correctIndex = 0,
            xpReward = 25
        ),
        TriviaQuestion(
            id = "triv_2",
            category = "Emoji Anime Quiz",
            promptText = "Decode the anime from these emojis!",
            hintOrEmoji = "🧝‍♀️🪄⏳💐🌌",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            options = listOf("Mushoku Tensei", "Frieren: Beyond Journey's End", "Black Clover", "Fairy Tail"),
            correctIndex = 1,
            xpReward = 30
        ),
        TriviaQuestion(
            id = "triv_3",
            category = "Guess the Anime",
            promptText = "Which anime features an E-Rank Hunter who awakens a secret leveling System in a Double Dungeon?",
            hintOrEmoji = "🗡️👤⬆️👑",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
            options = listOf("Tower of God", "Sword Art Online", "Solo Leveling", "Shangri-La Frontier"),
            correctIndex = 2,
            xpReward = 25
        ),
        TriviaQuestion(
            id = "triv_4",
            category = "Opening Quiz",
            promptText = "Which anime features the hit opening song 'Yuusha' performed by YOASOBI?",
            hintOrEmoji = "🎵 YOASOBI — Yuusha",
            imageUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            options = listOf("Oshi no Ko", "Frieren: Beyond Journey's End", "Beastars", "Mobile Suit Gundam: The Witch from Mercury"),
            correctIndex = 1,
            xpReward = 35
        ),
        TriviaQuestion(
            id = "triv_5",
            category = "Emoji Anime Quiz",
            promptText = "Guess this legendary pirate adventure from the emojis!",
            hintOrEmoji = "👒🏴‍☠️🍖🚢🌊",
            imageUrl = "https://cdn.myanimelist.net/images/anime/6/73245l.jpg",
            options = listOf("Vinland Saga", "One Piece", "Black Lagoon", "Edens Zero"),
            correctIndex = 1,
            xpReward = 25
        )
    )

    // 15. Episode Notes & Custom Video Bookmarks (Sections 11 & 12 of second prompt)
    private val _episodeNotes = MutableStateFlow(
        listOf(
            EpisodeNote("note_1", "anime_1", 1, "এই episode-এর endingটা crazy ছিল 😂 Frieren's time-skip hit right in the feels!"),
            EpisodeNote("note_2", "anime_2", 1, "Gojo & Geto's Hidden Inventory opening sequence sakuga is 10/10.")
        )
    )
    val episodeNotes: StateFlow<List<EpisodeNote>> = _episodeNotes.asStateFlow()

    private val _videoBookmarks = MutableStateFlow(
        listOf(
            VideoBookmark("bm_1", "anime_1", 1, 1122_000L, "Best fight"), // 18:42
            VideoBookmark("bm_2", "anime_1", 1, 1270_000L, "Important scene"), // 21:10
            VideoBookmark("bm_3", "anime_2", 1, 860_000L, "Domain Expansion climax") // 14:20
        )
    )
    val videoBookmarks: StateFlow<List<VideoBookmark>> = _videoBookmarks.asStateFlow()

    // 16. Smart Watch Queue (1. One Piece, 2. Bleach, 3. Naruto, 4. JJK)
    private val _watchQueue = MutableStateFlow(
        listOf(
            WatchQueueItem(1, "anime_5", "One Piece", "https://cdn.myanimelist.net/images/anime/6/73245l.jpg", 1122, 1150, "Toei Animation"),
            WatchQueueItem(2, "anime_3", "Solo Leveling", "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg", 4, 12, "A-1 Pictures"),
            WatchQueueItem(3, "anime_4", "Demon Slayer", "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg", 2, 11, "ufotable"),
            WatchQueueItem(4, "anime_2", "Jujutsu Kaisen", "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg", 8, 23, "MAPPA")
        )
    )
    val watchQueue: StateFlow<List<WatchQueueItem>> = _watchQueue.asStateFlow()

    // 17. Custom Watchlist Collections (Section 9)
    private val _customCollections = MutableStateFlow(
        listOf(
            WatchlistCollection("col_top10", "My Top 10", "All-time 10/10 masterpieces", "👑", listOf("anime_1", "anime_2", "anime_3"), isCustom = false),
            WatchlistCollection("col_shounen", "Shounen", "High-energy battle shounen favorites", "⚔️", listOf("anime_2", "anime_4", "anime_5"), isCustom = false),
            WatchlistCollection("col_romance", "Romance", "Wholesome & emotional romance picks", "💖", listOf("anime_1"), isCustom = false),
            WatchlistCollection("col_comedy", "Comedy", "Guaranteed laughs after a long day", "😂", listOf("anime_1", "anime_5"), isCustom = false),
            WatchlistCollection("col_action", "Action", "Peak sakuga fight choreography", "💥", listOf("anime_2", "anime_3", "anime_4"), isCustom = false),
            WatchlistCollection("col_rewatch", "Rewatch List", "Series worth rewatching in 1080p", "🔁", listOf("anime_1", "anime_2"), isCustom = false)
        )
    )
    val customCollections: StateFlow<List<WatchlistCollection>> = _customCollections.asStateFlow()

    fun toggleFollowUser(userId: String) {
        _socialUsers.update { list ->
            list.map { user ->
                if (user.userId == userId) user.copy(isFollowing = !user.isFollowing) else user
            }
        }
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "follow_$userId", "UPSERT", "Updated social follow graph")
    }

    fun postSocialActivity(username: String, avatarUrl: String, actionText: String, animeId: String? = null, animeTitle: String = "", ratingStars: Int? = null) {
        val item = SocialActivityItem(
            id = "act_${System.currentTimeMillis()}",
            userId = "u_default_01",
            username = username,
            userAvatar = avatarUrl,
            actionText = actionText,
            animeId = animeId,
            animeTitle = animeTitle,
            ratingStars = ratingStars,
            timestampLabel = "Just now"
        )
        _activityFeed.update { listOf(item) + it }
    }

    fun toggleLikeActivity(activityId: String) {
        _activityFeed.update { list ->
            list.map { act ->
                if (act.id == activityId) {
                    val newLiked = !act.isLiked
                    act.copy(
                        isLiked = newLiked,
                        likesCount = (act.likesCount + if (newLiked) 1 else -1).coerceAtLeast(0)
                    )
                } else act
            }
        }
    }

    fun toggleFavoriteCharacter(characterId: String) {
        _characters.update { list ->
            list.map { ch ->
                if (ch.id == characterId) {
                    val fav = !ch.isFavorite
                    ch.copy(isFavorite = fav, likesCount = ch.likesCount + if (fav) 1 else -1)
                } else ch
            }
        }
    }

    fun voteInPoll(pollId: String, optionId: String) {
        _communityPolls.update { list ->
            list.map { poll ->
                if (poll.id == pollId && poll.selectedOptionId == null) {
                    poll.copy(
                        selectedOptionId = optionId,
                        options = poll.options.map { opt ->
                            if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                        }
                    )
                } else poll
            }
        }
    }

    fun voteInBattle(battleId: String, side: String) {
        _animeBattles.update { list ->
            list.map { battle ->
                if (battle.id == battleId && battle.userVotedSide == null) {
                    if (side == "LEFT") {
                        battle.copy(leftVotes = battle.leftVotes + 1, userVotedSide = "LEFT")
                    } else {
                        battle.copy(rightVotes = battle.rightVotes + 1, userVotedSide = "RIGHT")
                    }
                } else battle
            }
        }
    }

    fun addEpisodeNote(animeId: String, episodeNumber: Int, noteText: String) {
        if (noteText.isBlank()) return
        val note = EpisodeNote(
            id = "note_${System.currentTimeMillis()}",
            animeId = animeId,
            episodeNumber = episodeNumber,
            noteText = noteText.trim()
        )
        _episodeNotes.update { listOf(note) + it }
        cloudSyncManager?.enqueueIncrementalSync("EPISODE_PROGRESS", note.id, "UPSERT", "Saved episode note")
    }

    fun deleteEpisodeNote(noteId: String) {
        _episodeNotes.update { list -> list.filterNot { it.id == noteId } }
    }

    fun addVideoBookmark(animeId: String, episodeNumber: Int, positionMs: Long, label: String) {
        val cleanLabel = label.trim().ifBlank { "Bookmarked scene" }
        val bm = VideoBookmark(
            id = "bm_${System.currentTimeMillis()}",
            animeId = animeId,
            episodeNumber = episodeNumber,
            positionMs = positionMs.coerceAtLeast(0L),
            label = cleanLabel
        )
        _videoBookmarks.update { (listOf(bm) + it).sortedBy { item -> item.positionMs } }
        cloudSyncManager?.enqueueIncrementalSync("EPISODE_PROGRESS", bm.id, "UPSERT", "Saved video bookmark at ${bm.formattedTimestamp}")
    }

    fun deleteVideoBookmark(bookmarkId: String) {
        _videoBookmarks.update { list -> list.filterNot { it.id == bookmarkId } }
    }

    fun addToWatchQueue(anime: Anime) {
        _watchQueue.update { current ->
            if (current.any { it.animeId == anime.id }) return@update current
            val nextOrder = current.size + 1
            current + WatchQueueItem(
                orderIndex = nextOrder,
                animeId = anime.id,
                animeTitle = anime.titleEnglish,
                posterUrl = anime.posterUrl,
                nextEpisodeNumber = 1,
                totalEpisodes = anime.episodesCount,
                studio = anime.studio
            )
        }
        cloudSyncManager?.enqueueIncrementalSync("WATCHLIST", "queue_${anime.id}", "UPSERT", "Added ${anime.titleEnglish} to Smart Watch Queue")
    }

    fun removeFromWatchQueue(animeId: String) {
        _watchQueue.update { current ->
            current.filterNot { it.animeId == animeId }
                .mapIndexed { idx, item -> item.copy(orderIndex = idx + 1) }
        }
    }

    fun moveQueueItemUp(animeId: String) {
        _watchQueue.update { current ->
            val idx = current.indexOfFirst { it.animeId == animeId }
            if (idx <= 0) return@update current
            val mutable = current.toMutableList()
            val item = mutable.removeAt(idx)
            mutable.add(idx - 1, item)
            mutable.mapIndexed { index, q -> q.copy(orderIndex = index + 1) }
        }
    }

    fun advanceWatchQueueAndGetNext(): WatchQueueItem? {
        val current = _watchQueue.value
        if (current.isEmpty()) return null
        val remaining = current.drop(1).mapIndexed { index, item -> item.copy(orderIndex = index + 1) }
        _watchQueue.value = remaining
        return remaining.firstOrNull()
    }

    fun createCustomCollection(name: String, description: String, emoji: String = "📁") {
        if (name.isBlank()) return
        val newCol = WatchlistCollection(
            id = "col_${System.currentTimeMillis()}",
            name = name.trim(),
            description = description.trim().ifBlank { "Custom user anime collection" },
            iconEmoji = emoji,
            animeIds = emptyList(),
            isCustom = true
        )
        _customCollections.update { it + newCol }
        cloudSyncManager?.enqueueIncrementalSync("CUSTOM_COLLECTIONS", newCol.id, "UPSERT", "Created collection ${newCol.name}")
    }

    fun addAnimeToCollection(collectionId: String, animeId: String) {
        _customCollections.update { list ->
            list.map { col ->
                if (col.id == collectionId && animeId !in col.animeIds) {
                    col.copy(animeIds = col.animeIds + animeId)
                } else col
            }
        }
        cloudSyncManager?.enqueueIncrementalSync("CUSTOM_COLLECTIONS", collectionId, "UPSERT", "Added $animeId to collection")
    }

    fun removeAnimeFromCollection(collectionId: String, animeId: String) {
        _customCollections.update { list ->
            list.map { col ->
                if (col.id == collectionId) {
                    col.copy(animeIds = col.animeIds.filterNot { it == animeId })
                } else col
            }
        }
    }

    fun deleteCustomCollection(collectionId: String) {
        _customCollections.update { list ->
            list.filterNot { it.id == collectionId && it.isCustom }
        }
    }

    fun claimChallengeReward(questId: String): Int {
        var awardedXp = 0
        _challenges.update { list ->
            list.map { q ->
                if (q.id == questId && q.isCompleted && !q.isClaimed) {
                    awardedXp = q.xpReward
                    q.copy(isClaimed = true)
                } else q
            }
        }
        return awardedXp
    }

    fun recordEpisodeWatchedProgress() {
        _challenges.update { list ->
            list.map { q ->
                if (q.title.contains("Episode", ignoreCase = true) && !q.isCompleted) {
                    val next = (q.currentCount + 1).coerceAtMost(q.targetCount)
                    q.copy(currentCount = next, isCompleted = next >= q.targetCount)
                } else q
            }
        }
        _achievements.update { list ->
            list.map { ach ->
                if (ach.category == "WATCH") {
                    val next = (ach.currentProgress + 1).coerceAtMost(ach.targetProgress)
                    ach.copy(currentProgress = next, isUnlocked = next >= ach.targetProgress)
                } else ach
            }
        }
    }

    fun getTrendingHeatmap(catalog: List<Anime>, period: String = "LIVE"): List<TrendingHeatItem> {
        val baseMultiplier = when (period) {
            "LIVE" -> 1
            "24H" -> 6
            "7D" -> 28
            else -> 95
        }
        return catalog.mapIndexed { idx, anime ->
            val viewers = ((12 - idx).coerceAtLeast(2) * 840 * baseMultiplier) + (anime.score * 12)
            val heat = (100 - (idx * 9)).coerceIn(35, 100)
            TrendingHeatItem(
                rank = idx + 1,
                anime = anime,
                activeViewers = viewers,
                heatScorePercent = heat,
                period = period
            )
        }
    }
}
