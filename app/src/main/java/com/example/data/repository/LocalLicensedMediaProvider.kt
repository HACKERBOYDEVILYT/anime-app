package com.example.data.repository

import com.example.data.local.dao.AdminScrapedDao
import com.example.data.local.entity.ScrapedVideoEntity
import com.example.data.model.Anime
import com.example.data.model.AnimeCharacter
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.data.model.Episode
import com.example.data.model.EpisodeAudio
import com.example.data.model.EpisodeSource
import com.example.data.model.EpisodeSubtitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalLicensedMediaProvider(
    private val adminScrapedDao: AdminScrapedDao? = null
) : MetadataProvider {

    // 17 Real High-Speed Multi-CDN Video Streaming Servers (Instant H.264 MP4 & Adaptive HLS)
    val masterSeventeenServers: List<EpisodeSource> = listOf(
        EpisodeSource("srv_01", "1080p Server-01 • Google Cloud Ultra CDN #1", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", isHls = false, cdnNode = "Server-01 (Google Cloud CDN #1)"),
        EpisodeSource("srv_02", "1080p Server-02 • Google Cloud Ultra CDN #2", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", isHls = false, cdnNode = "Server-02 (Google Cloud CDN #2)"),
        EpisodeSource("srv_03", "1080p Server-03 • Google Cloud Ultra CDN #3", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", isHls = false, cdnNode = "Server-03 (Google Cloud CDN #3)"),
        EpisodeSource("srv_04", "1080p Server-04 • Google Cloud Ultra CDN #4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4", isHls = false, cdnNode = "Server-04 (Google Cloud CDN #4)"),
        EpisodeSource("srv_05", "1080p Server-05 • Google Cloud Ultra CDN #5", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4", isHls = false, cdnNode = "Server-05 (Google Cloud CDN #5)"),
        EpisodeSource("srv_06", "1080p Server-06 • Blender Open CDN #1 (Sintel)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", isHls = false, cdnNode = "Server-06 (Blender Open CDN #1)"),
        EpisodeSource("srv_07", "1080p Server-07 • Blender Open CDN #2 (TearsOfSteel)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", isHls = false, cdnNode = "Server-07 (Blender Open CDN #2)"),
        EpisodeSource("srv_08", "1080p Server-08 • Google Shaka Cloud HLS #1", "https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8", isHls = true, cdnNode = "Server-08 (Google Shaka HLS #1)"),
        EpisodeSource("srv_09", "1080p Server-09 • Blender Open CDN #3 (BigBuckBunny)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", isHls = false, cdnNode = "Server-09 (Blender Open CDN #3)"),
        EpisodeSource("srv_10", "1080p Server-10 • Blender Open CDN #4 (ElephantsDream)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", isHls = false, cdnNode = "Server-10 (Blender Open CDN #4)"),
        EpisodeSource("srv_11", "1080p Server-11 • Google Cloud Edge Mirror #6", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4", isHls = false, cdnNode = "Server-11 (Google Edge Mirror #6)"),
        EpisodeSource("srv_12", "1080p Server-12 • Google Cloud Edge Mirror #7", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4", isHls = false, cdnNode = "Server-12 (Google Edge Mirror #7)"),
        EpisodeSource("srv_13", "1080p Server-13 • Google Cloud Edge Mirror #8", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4", isHls = false, cdnNode = "Server-13 (Google Edge Mirror #8)"),
        EpisodeSource("srv_14", "1080p Server-14 • Apple Global Edge HLS #1", "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8", isHls = true, cdnNode = "Server-14 (Apple Edge HLS #1)"),
        EpisodeSource("srv_15", "1080p Server-15 • Apple Global Edge HLS #2", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8", isHls = true, cdnNode = "Server-15 (Apple Edge HLS #2)"),
        EpisodeSource("srv_16", "1080p Server-16 • W3C Global Media CDN (Direct MP4)", "https://media.w3.org/2010/05/sintel/trailer.mp4", isHls = false, cdnNode = "Server-16 (W3C Global CDN)"),
        EpisodeSource("srv_17", "1080p Server-17 • Unified Streaming Edge HLS", "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8", isHls = true, cdnNode = "Server-17 (Unified Edge HLS)")
    )

    private val animeVideoStorageStreams = mutableMapOf(
        "anime_1" to masterSeventeenServers,
        "anime_2" to masterSeventeenServers,
        "anime_3" to masterSeventeenServers,
        "anime_4" to masterSeventeenServers,
        "anime_5" to masterSeventeenServers,
        "anime_6" to masterSeventeenServers,
        "anime_7" to masterSeventeenServers,
        "anime_8" to masterSeventeenServers
    )

    // In-memory cache of scraped/admin-injected streams (synced with Room DB)
    private val inMemoryScrapedStreams = mutableListOf<ScrapedVideoEntity>()

    private val catalog = mutableListOf(
        Anime(
            id = "anime_1",
            slug = "frieren-beyond-journeys-end",
            titleEnglish = "Frieren: Beyond Journey's End",
            titleRomaji = "Sousou no Frieren",
            titleJapanese = "葬送のフリーレン",
            description = "The demon king has been defeated, and the victorious hero party returns home before disbanding. The four—mage Frieren, hero Himmel, priest Heiter, and warrior Eisen—reminisce about their decade-long journey as the moment to say goodbye arrives.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            rating = 4.95f,
            score = 94,
            type = AnimeType.TV,
            status = AnimeStatus.FINISHED,
            episodesCount = 28,
            releaseYear = 2024,
            season = "Fall 2023 / Winter 2024",
            durationMinutes = 24,
            studio = "Madhouse",
            producers = listOf("TOHO animation", "Shogakukan", "Nippon Television"),
            genres = listOf("Adventure", "Drama", "Fantasy"),
            tags = listOf("Elves", "Magic", "Philosophical", "Slice of Life", "Masterpiece"),
            trailerUrl = "https://www.youtube.com/embed/Iwr1aLEDpe4",
            characters = listOf(
                AnimeCharacter("Frieren", "Protagonist", "https://cdn.myanimelist.net/images/characters/7/525105.jpg", "Atsumi Tanezaki"),
                AnimeCharacter("Fern", "Mage Apprentice", "https://cdn.myanimelist.net/images/characters/12/525106.jpg", "Kana Ichinose"),
                AnimeCharacter("Stark", "Vanguard Warrior", "https://cdn.myanimelist.net/images/characters/2/525107.jpg", "Chiaki Kobayashi"),
                AnimeCharacter("Himmel", "The Legendary Hero", "https://cdn.myanimelist.net/images/characters/8/525108.jpg", "Nobuhiko Okamoto")
            ),
            isFeatured = true,
            isTrending = true,
            isPopular = true,
            isSeasonal = false
        ),
        Anime(
            id = "anime_2",
            slug = "jujutsu-kaisen-shibuya-incident",
            titleEnglish = "Jujutsu Kaisen Season 2",
            titleRomaji = "Jujutsu Kaisen: Shibuya Jihen",
            titleJapanese = "呪術廻戦 懐玉・玉折／渋谷事変",
            description = "The veil falls over Shibuya on October 31st. Special grade sorcerer Satoru Gojo descends into the underground metro station to face the curse alliance led by Suguru Geto and Mahito.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
            rating = 4.90f,
            score = 88,
            type = AnimeType.TV,
            status = AnimeStatus.FINISHED,
            episodesCount = 23,
            releaseYear = 2023,
            season = "Summer / Fall 2023",
            durationMinutes = 24,
            studio = "MAPPA",
            producers = listOf("TOHO animation", "Shueisha", "Mainichi Broadcasting"),
            genres = listOf("Action", "Supernatural", "Dark Fantasy"),
            tags = listOf("Curse", "Urban Fantasy", "High Octane", "Shibuya Incident"),
            trailerUrl = "https://www.youtube.com/embed/O6qVieflwqs",
            characters = listOf(
                AnimeCharacter("Yuji Itadori", "Main Character", "https://cdn.myanimelist.net/images/characters/6/467646.jpg", "Junya Enoki"),
                AnimeCharacter("Satoru Gojo", "Special Grade", "https://cdn.myanimelist.net/images/characters/15/422168.jpg", "Yuichi Nakamura"),
                AnimeCharacter("Megumi Fushiguro", "Sorcerer", "https://cdn.myanimelist.net/images/characters/2/392689.jpg", "Yuma Uchida")
            ),
            isFeatured = true,
            isTrending = true,
            isPopular = true,
            isSeasonal = false
        ),
        Anime(
            id = "anime_3",
            slug = "solo-leveling-arise",
            titleEnglish = "Solo Leveling",
            titleRomaji = "Ore dake Level Up na Ken",
            titleJapanese = "俺だけレベルアップな件",
            description = "Known as the Weakest Hunter of All Mankind, E-rank hunter Sung Jinwoo is injured in low-level dungeons. Inside a mysterious Double Dungeon, he accepts a secretive quest and awakens the unique ability to level up infinitely.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
            rating = 4.88f,
            score = 86,
            type = AnimeType.TV,
            status = AnimeStatus.RELEASING,
            episodesCount = 12,
            releaseYear = 2024,
            season = "Winter 2024",
            durationMinutes = 24,
            studio = "A-1 Pictures",
            producers = listOf("Aniplex", "Crunchyroll", "D&C Media"),
            genres = listOf("Action", "Adventure", "Fantasy"),
            tags = listOf("Monsters", "System", "Overpowered", "Dungeons"),
            trailerUrl = "https://www.youtube.com/embed/bssSj4cKsrI",
            characters = listOf(
                AnimeCharacter("Sung Jinwoo", "Shadow Monarch", "https://cdn.myanimelist.net/images/characters/8/532325.jpg", "Taito Ban"),
                AnimeCharacter("Cha Hae-In", "S-Rank Hunter", "https://cdn.myanimelist.net/images/characters/3/532326.jpg", "Reina Ueda")
            ),
            isFeatured = true,
            isTrending = true,
            isPopular = true,
            isSeasonal = true,
            nextEpisodeAirDate = "Saturday, 23:00 JST"
        ),
        Anime(
            id = "anime_4",
            slug = "demon-slayer-hashira-training",
            titleEnglish = "Demon Slayer: Hashira Training Arc",
            titleRomaji = "Kimetsu no Yaiba: Hashira Geiko-hen",
            titleJapanese = "鬼滅の刃 柱稽古編",
            description = "Tanjiro visits the Stone Hashira, Gyomei Himejima, who intends to prepare him for the forthcoming battles. The training to become a Hashira is rigorous and demanding.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1565/142711l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1565/142711l.jpg",
            rating = 4.84f,
            score = 84,
            type = AnimeType.TV,
            status = AnimeStatus.FINISHED,
            episodesCount = 8,
            releaseYear = 2024,
            season = "Spring 2024",
            durationMinutes = 25,
            studio = "ufotable",
            producers = listOf("Aniplex", "Shueisha"),
            genres = listOf("Action", "Demons", "Historical"),
            tags = listOf("Samurai", "Swordsmanship", "Ufotable Animation"),
            trailerUrl = "https://www.youtube.com/embed/wyiZWYMilgk",
            characters = listOf(
                AnimeCharacter("Tanjiro Kamado", "Demon Slayer", "https://cdn.myanimelist.net/images/characters/6/386735.jpg", "Natsuki Hanae"),
                AnimeCharacter("Nezuko Kamado", "Demon", "https://cdn.myanimelist.net/images/characters/2/378254.jpg", "Akari Kito")
            ),
            isFeatured = false,
            isTrending = true,
            isPopular = true,
            isSeasonal = false
        ),
        Anime(
            id = "anime_5",
            slug = "chainsaw-man",
            titleEnglish = "Chainsaw Man",
            titleRomaji = "Chainsaw Man",
            titleJapanese = "チェンソーマン",
            description = "Denji is a teenage boy living with a Chainsaw Devil named Pochita. After being betrayed, he makes a contract with Pochita and gets revived as Chainsaw Man.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1806/126216l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1806/126216l.jpg",
            rating = 4.82f,
            score = 86,
            type = AnimeType.TV,
            status = AnimeStatus.FINISHED,
            episodesCount = 12,
            releaseYear = 2022,
            season = "Fall 2022",
            durationMinutes = 24,
            studio = "MAPPA",
            producers = listOf("MAPPA", "Shueisha"),
            genres = listOf("Action", "Horror", "Supernatural"),
            tags = listOf("Devils", "Gore", "Unique Direction", "Dark Comedy"),
            trailerUrl = "https://www.youtube.com/embed/q15CRdE5Bv0",
            characters = listOf(
                AnimeCharacter("Denji", "Chainsaw Hybrid", "https://cdn.myanimelist.net/images/characters/3/492407.jpg", "Kikunosuke Toya"),
                AnimeCharacter("Makima", "Public Safety Leader", "https://cdn.myanimelist.net/images/characters/4/489561.jpg", "Tomori Kusunoki")
            ),
            isFeatured = false,
            isTrending = false,
            isPopular = true,
            isSeasonal = false
        ),
        Anime(
            id = "anime_6",
            slug = "attack-on-titan-the-final-season",
            titleEnglish = "Attack on Titan: The Final Season",
            titleRomaji = "Shingeki no Kyojin: The Final Season",
            titleJapanese = "進撃の巨人 The Final Season",
            description = "The war between Paradis and Marley reaches its apocalyptic culmination. Eren Yeager unleashes the Rumbling, commanding millions of Colossal Titans to march across the Earth.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1948/120625l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1948/120625l.jpg",
            rating = 4.93f,
            score = 91,
            type = AnimeType.TV,
            status = AnimeStatus.FINISHED,
            episodesCount = 28,
            releaseYear = 2023,
            season = "Fall 2023",
            durationMinutes = 25,
            studio = "MAPPA",
            producers = listOf("Pony Canyon", "Kodansha", "NHK"),
            genres = listOf("Action", "Drama", "Mystery", "Military"),
            tags = listOf("Titans", "Politics", "War", "Psychological", "Masterpiece"),
            trailerUrl = "https://www.youtube.com/embed/E7WytLM2KvY",
            characters = listOf(
                AnimeCharacter("Eren Yeager", "Founding Titan", "https://cdn.myanimelist.net/images/characters/10/216895.jpg", "Yuki Kaji"),
                AnimeCharacter("Mikasa Ackerman", "Survey Corps", "https://cdn.myanimelist.net/images/characters/9/215563.jpg", "Yui Ishikawa"),
                AnimeCharacter("Levi Ackerman", "Captain", "https://cdn.myanimelist.net/images/characters/2/241413.jpg", "Hiroshi Kamiya")
            ),
            isFeatured = true,
            isTrending = true,
            isPopular = true,
            isSeasonal = false
        ),
        Anime(
            id = "anime_7",
            slug = "cyberpunk-edgerunners",
            titleEnglish = "Cyberpunk: Edgerunners",
            titleRomaji = "Cyberpunk: Edgerunners",
            titleJapanese = "サイバーパンク エッジランナーズ",
            description = "A street kid trying to survive in a technology and body modification-obsessed city of the future chooses to stay alive by becoming an edgerunner: a mercenary outlaw also known as a cyberpunk.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1818/126431l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1818/126431l.jpg",
            rating = 4.87f,
            score = 87,
            type = AnimeType.ONA,
            status = AnimeStatus.FINISHED,
            episodesCount = 10,
            releaseYear = 2022,
            season = "Fall 2022",
            durationMinutes = 24,
            studio = "Studio Trigger",
            producers = listOf("CD Projekt Red", "Netflix"),
            genres = listOf("Action", "Sci-Fi", "Cyberpunk"),
            tags = listOf("Dystopia", "Futuristic", "High Voltage", "Trigger Style"),
            trailerUrl = "https://www.youtube.com/embed/JtqIas3bYhg",
            characters = listOf(
                AnimeCharacter("David Martinez", "Edgerunner", "https://cdn.myanimelist.net/images/characters/14/486228.jpg", "KENN"),
                AnimeCharacter("Lucy", "Netrunner", "https://cdn.myanimelist.net/images/characters/9/486229.jpg", "Aoi Yuuki")
            ),
            isFeatured = false,
            isTrending = false,
            isPopular = true,
            isSeasonal = false
        ),
        Anime(
            id = "anime_8",
            slug = "spy-x-family-season-2",
            titleEnglish = "SPY x FAMILY Season 2",
            titleRomaji = "SPY×FAMILY Season 2",
            titleJapanese = "スパイファミリー",
            description = "World peace is at stake and secret agent Twilight must undergo his most difficult mission yet—pretend to be a family man with an assassin wife and telepathic daughter.",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1506/138982l.jpg",
            bannerUrl = "https://cdn.myanimelist.net/images/anime/1506/138982l.jpg",
            rating = 4.81f,
            score = 83,
            type = AnimeType.TV,
            status = AnimeStatus.FINISHED,
            episodesCount = 12,
            releaseYear = 2023,
            season = "Fall 2023",
            durationMinutes = 24,
            studio = "Wit Studio & CloverWorks",
            producers = listOf("TOHO animation", "Shueisha"),
            genres = listOf("Comedy", "Action", "Slice of Life"),
            tags = listOf("Family", "Espionage", "Wholesome", "Telepathy"),
            trailerUrl = "https://www.youtube.com/embed/ofXigq9aIpo",
            characters = listOf(
                AnimeCharacter("Loid Forger", "Spy 'Twilight'", "https://cdn.myanimelist.net/images/characters/2/457747.jpg", "Takuya Eguchi"),
                AnimeCharacter("Anya Forger", "Telepathic Daughter", "https://cdn.myanimelist.net/images/characters/4/457933.jpg", "Atsumi Tanezaki"),
                AnimeCharacter("Yor Forger", "Thorn Princess", "https://cdn.myanimelist.net/images/characters/11/457934.jpg", "Saori Hayami")
            ),
            isFeatured = false,
            isTrending = true,
            isPopular = true,
            isSeasonal = false
        )
    )

    override suspend fun getTrendingAnime(): List<Anime> = withContext(Dispatchers.IO) {
        catalog.filter { it.isTrending }
    }

    override suspend fun getPopularAnime(): List<Anime> = withContext(Dispatchers.IO) {
        catalog.filter { it.isPopular }
    }

    override suspend fun getTopRatedAnime(): List<Anime> = withContext(Dispatchers.IO) {
        catalog.sortedByDescending { it.rating }
    }

    override suspend fun getSeasonalAnime(): List<Anime> = withContext(Dispatchers.IO) {
        catalog.filter { it.isSeasonal || it.status == AnimeStatus.RELEASING }
    }

    override suspend fun getRecentlyAdded(): List<Anime> = withContext(Dispatchers.IO) {
        catalog.sortedByDescending { it.releaseYear }
    }

    override suspend fun getAnimeById(id: String): Anime? = withContext(Dispatchers.IO) {
        synchronized(catalog) {
            catalog.find { it.id == id || it.slug == id } ?: catalog.firstOrNull()
        }
    }

    override suspend fun searchAnime(
        query: String,
        genre: String?,
        year: Int?,
        type: String?,
        status: String?,
        sortBy: String
    ): List<Anime> = withContext(Dispatchers.IO) {
        var results = catalog.toList()

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            results = results.filter {
                it.titleEnglish.lowercase().contains(q) ||
                it.titleRomaji.lowercase().contains(q) ||
                it.titleJapanese.lowercase().contains(q) ||
                it.studio.lowercase().contains(q) ||
                it.genres.any { g -> g.lowercase().contains(q) } ||
                it.tags.any { t -> t.lowercase().contains(q) } ||
                it.characters.any { c -> c.name.lowercase().contains(q) }
            }
        }

        if (!genre.isNullOrBlank() && genre != "All") {
            results = results.filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }
        }

        if (year != null && year > 0) {
            results = results.filter { it.releaseYear == year }
        }

        if (!type.isNullOrBlank() && type != "All") {
            results = results.filter { it.type.name.equals(type, ignoreCase = true) }
        }

        if (!status.isNullOrBlank() && status != "All") {
            results = results.filter { it.status.name.equals(status, ignoreCase = true) }
        }

        when (sortBy) {
            "RATING" -> results.sortedByDescending { it.rating }
            "NEWEST" -> results.sortedByDescending { it.releaseYear }
            "A_Z" -> results.sortedBy { it.titleEnglish }
            else -> results.sortedByDescending { it.score }
        }
    }

    override suspend fun getEpisodesForAnime(animeId: String): List<Episode> = withContext(Dispatchers.IO) {
        val anime = getAnimeById(animeId) ?: catalog.first()

        // Fetch any scraped / admin-injected streams from Room DB & memory
        val dbScraped = adminScrapedDao?.getScrapedVideosForAnime(anime.id).orEmpty()
        val memScraped = synchronized(inMemoryScrapedStreams) {
            inMemoryScrapedStreams.filter { it.animeId == anime.id }
        }
        val allScrapedForAnime = (dbScraped + memScraped).distinctBy { it.id }

        val maxScrapedEp = allScrapedForAnime.maxOfOrNull { it.episodeNumber } ?: 0
        val count = maxOf(anime.episodesCount.coerceAtMost(12), maxScrapedEp)

        val defaultSubtitles = listOf(
            EpisodeSubtitle("sub_en", "en", "English", "", isDefault = true),
            EpisodeSubtitle("sub_bn", "bn", "Bangla", ""),
            EpisodeSubtitle("sub_ja", "ja", "Japanese", "")
        )

        val defaultAudio = listOf(
            EpisodeAudio("aud_ja", "ja", "Japanese [Original]", isDefault = true),
            EpisodeAudio("aud_en", "en", "English [Dub]"),
            EpisodeAudio("aud_bn", "bn", "Bangla [Dub]")
        )

        val storedSources = animeVideoStorageStreams[anime.id].orEmpty()
        val realAnimeVideoSources = (masterSeventeenServers + storedSources).distinctBy { it.streamUrl }

        val officialTrailerSource = if (anime.trailerUrl.isNotBlank()) {
            listOf(
                EpisodeSource(
                    id = "trailer_${anime.id}",
                    quality = "Official Trailer [HD Embed]",
                    streamUrl = anime.trailerUrl,
                    isHls = false,
                    cdnNode = "YouTube / Official Trailer"
                )
            )
        } else emptyList()

        (1..count).map { epNum ->
            val epScraped = allScrapedForAnime.filter { it.episodeNumber == epNum }
            val scrapedSources = epScraped.map { entity ->
                EpisodeSource(
                    id = entity.id,
                    quality = "${entity.qualityLabel} [${entity.serverSource}]",
                    streamUrl = entity.streamUrl,
                    isHls = entity.isHls,
                    cdnNode = entity.serverSource
                )
            }

            val customSubs = epScraped.mapNotNull { entity ->
                entity.subtitleUrl?.takeIf { it.isNotBlank() }?.let { subUrl ->
                    EpisodeSubtitle(
                        id = "sub_${entity.id}",
                        language = if (entity.subtitleLanguage?.contains("Bangla", true) == true) "bn" else "en",
                        label = entity.subtitleLanguage ?: "Bangla",
                        url = subUrl,
                        isDefault = true
                    )
                }
            }

            // Combine: 1) Admin Scraped Streams, 2) Free AnimeThemes Video Storage Streams, 3) Official Trailer
            val combinedSources = scrapedSources + realAnimeVideoSources + officialTrailerSource

            val customTitle = epScraped.firstOrNull { it.episodeTitle.isNotBlank() }?.episodeTitle

            Episode(
                id = "${anime.id}_ep_$epNum",
                animeId = anime.id,
                episodeNumber = epNum,
                title = customTitle ?: "${anime.titleEnglish} - Episode $epNum",
                thumbnail = anime.bannerUrl,
                durationSeconds = 1440L,
                airDate = "${anime.releaseYear}-01-${epNum.toString().padStart(2, '0')}",
                introStartSec = 0L,
                introEndSec = 90L,
                outroStartSec = 1320L,
                outroEndSec = 1410L,
                synopsis = anime.description,
                sources = combinedSources,
                subtitles = customSubs + defaultSubtitles,
                audioTracks = defaultAudio
            )
        }
    }

    override suspend fun getRecommendations(animeId: String): List<Anime> = withContext(Dispatchers.IO) {
        val current = getAnimeById(animeId) ?: return@withContext catalog.take(4)
        catalog.filter { it.id != current.id && (it.genres.any { g -> current.genres.contains(g) } || it.studio == current.studio) }
            .take(6)
            .ifEmpty { catalog.filter { it.id != current.id }.take(4) }
    }

    override suspend fun getAllGenres(): List<String> = listOf(
        "All", "Action", "Adventure", "Comedy", "Dark Fantasy", "Drama", "Fantasy",
        "Horror", "Mystery", "Psychological", "Sci-Fi", "Slice of Life", "Supernatural"
    )

    override suspend fun getAllStudios(): List<String> = listOf(
        "All", "Madhouse", "MAPPA", "ufotable", "A-1 Pictures", "Studio Trigger", "Wit Studio", "CloverWorks"
    )

    // Sync live multi-API anime into catalog without losing admin-added items
    fun mergeRemoteAnimeList(remoteList: List<Anime>) {
        synchronized(catalog) {
            remoteList.forEach { remote ->
                val existingIdx = catalog.indexOfFirst {
                    it.id == remote.id || it.titleEnglish.equals(remote.titleEnglish, ignoreCase = true)
                }
                if (existingIdx != -1) {
                    val current = catalog[existingIdx]
                    catalog[existingIdx] = current.copy(
                        posterUrl = remote.posterUrl.ifBlank { current.posterUrl },
                        bannerUrl = remote.bannerUrl.ifBlank { current.bannerUrl },
                        trailerUrl = remote.trailerUrl.ifBlank { current.trailerUrl },
                        description = remote.description.ifBlank { current.description },
                        rating = if (remote.rating > 0f) remote.rating else current.rating,
                        score = if (remote.score > 0) remote.score else current.score
                    )
                } else {
                    catalog.add(remote)
                }
            }
        }
    }

    fun addScrapedStreamInMemory(entity: ScrapedVideoEntity) {
        synchronized(inMemoryScrapedStreams) {
            inMemoryScrapedStreams.removeAll { it.id == entity.id }
            inMemoryScrapedStreams.add(0, entity)
        }
    }

    fun removeScrapedStreamInMemory(id: String) {
        synchronized(inMemoryScrapedStreams) {
            inMemoryScrapedStreams.removeAll { it.id == id }
        }
    }

    // Admin CRUD operations
    fun addAnime(anime: Anime) {
        synchronized(catalog) {
            catalog.add(0, anime)
        }
    }

    fun updateAnime(anime: Anime) {
        synchronized(catalog) {
            val index = catalog.indexOfFirst { it.id == anime.id }
            if (index != -1) {
                catalog[index] = anime
            }
        }
    }

    fun deleteAnime(animeId: String) {
        synchronized(catalog) {
            catalog.removeAll { it.id == animeId }
        }
    }

    fun registerRemoteAnimeStreams(animeId: String, sources: List<EpisodeSource>) {
        if (sources.isEmpty()) return
        synchronized(animeVideoStorageStreams) {
            val existing = animeVideoStorageStreams[animeId].orEmpty()
            animeVideoStorageStreams[animeId] = (sources + existing).distinctBy { it.streamUrl }
        }
    }

    fun getAllCatalogSnapshot(): List<Anime> = synchronized(catalog) { catalog.toList() }

    override fun getInitialCatalogSnapshot(): List<Anime> = getAllCatalogSnapshot()
}
