package com.example.data.repository

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

class LocalLicensedMediaProvider : MetadataProvider {

    // Standard authorized multi-bitrate HLS streams for video streaming pipeline
    private val defaultHlsMaster = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private val hlsStream1080p = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    private val hlsStream720p = "https://test-streams.mux.dev/x36xhzz/url_2/193039199_mp4_h264_aac_hd_720p.m3u8"
    private val hlsStream480p = "https://test-streams.mux.dev/x36xhzz/url_1/193039199_mp4_h264_aac_hq_480p.m3u8"
    private val hlsTearsOfSteel = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    private val hlsSintel = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"

    private val catalog = mutableListOf(
        Anime(
            id = "anime_1",
            slug = "frieren-beyond-journeys-end",
            titleEnglish = "Frieren: Beyond Journey's End",
            titleRomaji = "Sousou no Frieren",
            titleJapanese = "葬送のフリーレン",
            description = "The demon king has been defeated, and the victorious hero party returns home before disbanding. The four—mage Frieren, hero Himmel, priest Heiter, and warrior Eisen—reminisce about their decade-long journey as the moment to say goodbye arrives. But the passing of time is different for elves, and Frieren witnesses her companions slowly pass away one by one.",
            posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = 4.95f,
            score = 96,
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
            trailerUrl = "https://www.youtube.com/watch?v=qgQunxD0qMo",
            characters = listOf(
                AnimeCharacter("Frieren", "Protagonist", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Atsumi Tanezaki"),
                AnimeCharacter("Fern", "Mage Apprentice", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "Kana Ichinose"),
                AnimeCharacter("Stark", "Vanguard Warrior", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200", "Chiaki Kobayashi"),
                AnimeCharacter("Himmel", "The Legendary Hero", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "Nobuhiko Okamoto")
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
            titleJapanese = "呪術廻戦",
            description = "The veil falls over Shibuya on October 31st. Special grade sorcerer Satoru Gojo descends into the underground metro station to face the curse alliance led by Suguru Geto and Mahito. A catastrophic battle ensues that will reshape the jujutsu world forever.",
            posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1200&q=80",
            rating = 4.90f,
            score = 94,
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
            characters = listOf(
                AnimeCharacter("Yuji Itadori", "Main Character", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", "Junya Enoki"),
                AnimeCharacter("Satoru Gojo", "Special Grade", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "Yuichi Nakamura"),
                AnimeCharacter("Megumi Fushiguro", "Sorcerer", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200", "Yuma Uchida"),
                AnimeCharacter("Nobara Kugisaki", "Sorcerer", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200", "Asami Seto")
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
            description = "Known as the Weakest Hunter of All Mankind, E-rank hunter Sung Jinwoo is injured in low-level dungeons. But inside a mysterious Double Dungeon, he accepts a secretive quest and awakens the unique ability to level up infinitely.",
            posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?auto=format&fit=crop&w=1200&q=80",
            rating = 4.88f,
            score = 91,
            type = AnimeType.TV,
            status = AnimeStatus.RELEASING,
            episodesCount = 24,
            releaseYear = 2026,
            season = "Winter 2026",
            durationMinutes = 24,
            studio = "A-1 Pictures",
            producers = listOf("Aniplex", "Crunchyroll", "D&C Media"),
            genres = listOf("Action", "Adventure", "Fantasy"),
            tags = listOf("Monsters", "System", "Overpowered", "Dungeons"),
            characters = listOf(
                AnimeCharacter("Sung Jinwoo", "Shadow Monarch", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "Taito Ban"),
                AnimeCharacter("Cha Hae-In", "S-Rank Hunter", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Reina Ueda")
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
            description = "Tanjiro visits the Stone Hashira, Gyomei Himejima, who intends to prepare him for the forthcoming battles. The training to become a Hashira is rigorous and demanding, earning Gyomei's approval seems impossible.",
            posterUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = 4.84f,
            score = 90,
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
            characters = listOf(
                AnimeCharacter("Tanjiro Kamado", "Demon Slayer", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", "Natsuki Hanae"),
                AnimeCharacter("Nezuko Kamado", "Demon", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "Akari Kito")
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
            description = "Denji is a teenage boy living with a Chainsaw Devil named Pochita. Due to the debt his father left behind, he has been living a rock-bottom life while repaying his debt by harvesting devil corpses with Pochita. One day, Denji is betrayed and killed. As his consciousness fades, he makes a contract with Pochita and gets revived as Chainsaw Man.",
            posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=1200&q=80",
            rating = 4.82f,
            score = 89,
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
            characters = listOf(
                AnimeCharacter("Denji", "Chainsaw Hybrid", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "Kikunosuke Toya"),
                AnimeCharacter("Makima", "Public Safety Leader", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Tomori Kusunoki")
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
            description = "The war between Paradis and Marley reaches its apocalyptic culmination. Eren Yeager unleashes the Rumbling, commanding millions of Colossal Titans to march across the Earth and exterminate all life outside Paradis Island. His former comrades must unite to stop him.",
            posterUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1200&q=80",
            rating = 4.93f,
            score = 95,
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
            characters = listOf(
                AnimeCharacter("Eren Yeager", "Founding Titan", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", "Yuki Kaji"),
                AnimeCharacter("Mikasa Ackerman", "Survey Corps", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "Yui Ishikawa"),
                AnimeCharacter("Levi Ackerman", "Captain", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "Hiroshi Kamiya")
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
            description = "A street kid trying to survive in a technology and body modification-obsessed city of the future. Having everything to lose, he chooses to stay alive by becoming an edgerunner: a mercenary outlaw also known as a cyberpunk.",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80",
            rating = 4.87f,
            score = 92,
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
            characters = listOf(
                AnimeCharacter("David Martinez", "Edgerunner", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "KENN"),
                AnimeCharacter("Lucy", "Netrunner", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Aoi Yuuki")
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
            description = "World peace is at stake and secret agent Twilight must undergo his most difficult mission yet—pretend to be a family man. Posing as the loving husband and father, he will infiltrate an elite school to get close to a high-profile politician. He has the perfect cover, except his wife's a deadly assassin and neither knows each other's secret. But someone does, his adopted daughter who's a telepath!",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?auto=format&fit=crop&w=1200&q=80",
            rating = 4.81f,
            score = 88,
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
            characters = listOf(
                AnimeCharacter("Loid Forger", "Spy 'Twilight'", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "Takuya Eguchi"),
                AnimeCharacter("Anya Forger", "Telepathic Daughter", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "Atsumi Tanezaki"),
                AnimeCharacter("Yor Forger", "Thorn Princess", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200", "Saori Hayami")
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
        catalog.find { it.id == id || it.slug == id }
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
        val count = anime.episodesCount.coerceAtMost(12) // Provide detailed episodes up to 12

        val subList = listOf(
            EpisodeSubtitle("sub_en", "en", "English", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_en.vtt", isDefault = true),
            EpisodeSubtitle("sub_ja", "ja", "Japanese", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_ja.vtt"),
            EpisodeSubtitle("sub_es", "es", "Spanish", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_es.vtt"),
            EpisodeSubtitle("sub_fr", "fr", "French", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_fr.vtt"),
            EpisodeSubtitle("sub_bn", "bn", "Bangla", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_bn.vtt"),
            EpisodeSubtitle("sub_hi", "hi", "Hindi", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_hi.vtt"),
            EpisodeSubtitle("sub_ar", "ar", "Arabic", "https://bitmovin-a.akamaihd.net/content/sintel/subtitles/subtitles_ar.vtt")
        )

        val audioList = listOf(
            EpisodeAudio("aud_ja", "ja", "Japanese [Original]", isDefault = true),
            EpisodeAudio("aud_en", "en", "English [Dub]"),
            EpisodeAudio("aud_hi", "hi", "Hindi [Dub]"),
            EpisodeAudio("aud_bn", "bn", "Bangla [Dub]"),
            EpisodeAudio("aud_es", "es", "Spanish [Dub]")
        )

        (1..count).map { epNum ->
            val videoSources = listOf(
                EpisodeSource("src_auto", "Auto", hlsStream1080p, isHls = true),
                EpisodeSource("src_1080p", "1080p Ultra HD", hlsStream1080p, isHls = true),
                EpisodeSource("src_720p", "720p High Def", hlsStream720p, isHls = true),
                EpisodeSource("src_480p", "480p Standard", hlsStream480p, isHls = true),
                EpisodeSource("src_direct", "Direct MP4 Backup", defaultHlsMaster, isHls = false)
            )

            Episode(
                id = "${anime.id}_ep_$epNum",
                animeId = anime.id,
                episodeNumber = epNum,
                title = when (epNum) {
                    1 -> "The Journey's Beginning"
                    2 -> "It Didn't Have to Be Magic..."
                    3 -> "Killing Magic"
                    4 -> "The Land Where Souls Rest"
                    5 -> "Phantom of the Dead"
                    6 -> "The Hero of the Village"
                    else -> "Episode $epNum: The Path Forward"
                },
                thumbnail = anime.bannerUrl,
                durationSeconds = 1440L,
                airDate = "2024-01-0$epNum",
                introStartSec = 85L,
                introEndSec = 175L,
                outroStartSec = 1320L,
                outroEndSec = 1410L,
                synopsis = "The adventures and trials of the party continue as new encounters challenge their understanding of life, magic, and bond.",
                sources = videoSources,
                subtitles = subList,
                audioTracks = audioList
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

    // Admin CRUD operations
    fun addAnime(anime: Anime) {
        catalog.add(0, anime)
    }

    fun updateAnime(anime: Anime) {
        val index = catalog.indexOfFirst { it.id == anime.id }
        if (index != -1) {
            catalog[index] = anime
        }
    }

    fun deleteAnime(animeId: String) {
        catalog.removeAll { it.id == animeId }
    }
}
