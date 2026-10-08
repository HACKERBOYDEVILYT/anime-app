package com.example.data.repository

import android.util.Log
import com.example.data.model.Anime
import com.example.data.model.AnimeCharacter
import com.example.data.model.AnimeStatus
import com.example.data.model.AnimeType
import com.example.data.model.Episode
import com.example.data.model.EpisodeSource
import com.example.data.network.CatalogNetworkMonitor
import com.example.data.network.KuroApiService
import com.example.data.network.RetrofitClient
import com.example.data.network.model.AnimeDto
import com.example.data.network.model.EpisodeDto
import com.example.data.network.model.PlaybackSessionResponse
import com.example.data.network.model.StreamAuthorizationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID

/**
 * Multi-Server API Metadata & Video Stream Provider.
 * Integrates:
 * 1) Admin-injected Scraped Videos & Free Storage Server streams
 * 2) Jikan v4 Free REST API (MyAnimeList Official Catalog — All Types: TV, Movie, OVA, ONA, Special, Music)
 * 3) AniList Free GraphQL API (Full Anime GraphQL Search, Trending, Airing, Characters, Synonyms & Formats)
 * 4) AnimeThemes Free Video Storage Server (Direct .webm/.mp4 Anime Streams)
 * 5) Custom Configured KuroApiService Endpoints
 */
class RetrofitMetadataProvider(
    private val apiService: KuroApiService,
    private val fallbackProvider: LocalLicensedMediaProvider = LocalLicensedMediaProvider(),
    private val catalogNetworkMonitor: CatalogNetworkMonitor = CatalogNetworkMonitor.getInstance()
) : MetadataProvider {

    private val tag = "MultiServerApiProvider"

    private fun normalizeTitleForMatch(raw: String): String {
        return raw.lowercase()
            .replace("é", "e")
            .replace("ū", "u")
            .replace("ō", "o")
            .replace("×", "x")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }

    override fun getInitialCatalogSnapshot(): List<Anime> = fallbackProvider.getInitialCatalogSnapshot()

    override suspend fun getTrendingAnime(): List<Anime> = withContext(Dispatchers.IO) {
        if (!catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Trending Catalog (Jikan v4 & AniList GraphQL)")) {
            return@withContext fallbackProvider.getTrendingAnime()
        }

        coroutineScope {
            val animeThemesDeferred = async {
                fetchFromAnimeThemesVideoServer("https://api.animethemes.moe/anime?include=animethemes.animethemeentries.videos,images&sort=-year&page[size]=8")
            }
            val jikanDeferred = async {
                fetchFromJikanApi("https://api.jikan.moe/v4/top/anime?filter=airing&limit=20")
            }
            val anilistDeferred = async {
                fetchFromAniListGraphQl(sort = "TRENDING_DESC", perPage = 20)
            }

            val animeThemesList = animeThemesDeferred.await()
            val jikanList = jikanDeferred.await()
            val anilist = anilistDeferred.await()

            if (animeThemesList.isNotEmpty()) {
                fallbackProvider.mergeRemoteAnimeList(animeThemesList)
            }
            if (jikanList.isNotEmpty()) {
                fallbackProvider.mergeRemoteAnimeList(jikanList)
            }
            if (anilist.isNotEmpty()) {
                fallbackProvider.mergeRemoteAnimeList(anilist)
            }

            if (jikanList.isNotEmpty() || anilist.isNotEmpty() || animeThemesList.isNotEmpty()) {
                catalogNetworkMonitor.reportCatalogFetchSuccess()
            } else {
                catalogNetworkMonitor.reportCatalogFetchFailure(
                    sourceLabel = "Trending Catalog API",
                    errorDetail = "Could not reach Jikan v4, AniList GraphQL, or AnimeThemes catalog servers"
                )
            }
        }

        fallbackProvider.getTrendingAnime()
    }

    override suspend fun getPopularAnime(): List<Anime> = withContext(Dispatchers.IO) {
        if (!catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Popular Catalog (Jikan v4 & AniList GraphQL)")) {
            return@withContext fallbackProvider.getPopularAnime()
        }
        coroutineScope {
            val jikanDeferred = async {
                fetchFromJikanApi("https://api.jikan.moe/v4/top/anime?filter=bypopularity&limit=20")
            }
            val anilistDeferred = async {
                fetchFromAniListGraphQl(sort = "POPULARITY_DESC", perPage = 20)
            }
            val jikanList = jikanDeferred.await()
            val anilistList = anilistDeferred.await()
            if (jikanList.isNotEmpty()) fallbackProvider.mergeRemoteAnimeList(jikanList)
            if (anilistList.isNotEmpty()) fallbackProvider.mergeRemoteAnimeList(anilistList)
            if (jikanList.isNotEmpty() || anilistList.isNotEmpty()) {
                catalogNetworkMonitor.reportCatalogFetchSuccess()
            }
        }
        fallbackProvider.getPopularAnime()
    }

    override suspend fun getTopRatedAnime(): List<Anime> = withContext(Dispatchers.IO) {
        if (!catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Top Rated Catalog (Jikan v4 & AniList GraphQL)")) {
            return@withContext fallbackProvider.getTopRatedAnime()
        }
        coroutineScope {
            val jikanDeferred = async {
                fetchFromJikanApi("https://api.jikan.moe/v4/top/anime?limit=20")
            }
            val anilistDeferred = async {
                fetchFromAniListGraphQl(sort = "SCORE_DESC", perPage = 20)
            }
            val jikanList = jikanDeferred.await()
            val anilistList = anilistDeferred.await()
            if (jikanList.isNotEmpty()) fallbackProvider.mergeRemoteAnimeList(jikanList)
            if (anilistList.isNotEmpty()) fallbackProvider.mergeRemoteAnimeList(anilistList)
            if (jikanList.isNotEmpty() || anilistList.isNotEmpty()) {
                catalogNetworkMonitor.reportCatalogFetchSuccess()
            }
        }
        fallbackProvider.getTopRatedAnime()
    }

    override suspend fun getSeasonalAnime(): List<Anime> = withContext(Dispatchers.IO) {
        if (!catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Seasonal Simulcast Catalog")) {
            return@withContext fallbackProvider.getSeasonalAnime()
        }
        val jikanSeasonal = fetchFromJikanApi("https://api.jikan.moe/v4/seasons/now?limit=20")
        if (jikanSeasonal.isNotEmpty()) {
            fallbackProvider.mergeRemoteAnimeList(jikanSeasonal)
            catalogNetworkMonitor.reportCatalogFetchSuccess()
        }
        fallbackProvider.getSeasonalAnime()
    }

    override suspend fun getRecentlyAdded(): List<Anime> = withContext(Dispatchers.IO) {
        if (!catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Recently Added Video Catalog")) {
            return@withContext fallbackProvider.getRecentlyAdded()
        }
        val recentVideoAnime = fetchFromAnimeThemesVideoServer("https://api.animethemes.moe/anime?include=animethemes.animethemeentries.videos,images&sort=-updated_at&page[size]=10")
        if (recentVideoAnime.isNotEmpty()) {
            fallbackProvider.mergeRemoteAnimeList(recentVideoAnime)
            catalogNetworkMonitor.reportCatalogFetchSuccess()
        }
        fallbackProvider.getRecentlyAdded()
    }

    override suspend fun getAnimeById(id: String): Anime? = withContext(Dispatchers.IO) {
        val existing = fallbackProvider.getAnimeById(id)
        if (existing != null) return@withContext existing

        if (id.startsWith("mal_")) {
            val malId = id.removePrefix("mal_").toIntOrNull()
            if (malId != null) {
                val singleList = fetchSingleJikanAnimeById(malId)
                if (singleList.isNotEmpty()) {
                    fallbackProvider.mergeRemoteAnimeList(singleList)
                    return@withContext fallbackProvider.getAnimeById(id) ?: singleList.first()
                }
            }
        } else if (id.startsWith("anilist_")) {
            val aniId = id.removePrefix("anilist_").toIntOrNull()
            if (aniId != null) {
                val singleList = fetchSingleAniListAnimeById(aniId)
                if (singleList.isNotEmpty()) {
                    fallbackProvider.mergeRemoteAnimeList(singleList)
                    return@withContext fallbackProvider.getAnimeById(id) ?: singleList.first()
                }
            }
        }
        null
    }

    override suspend fun searchAnime(
        query: String,
        genre: String?,
        year: Int?,
        type: String?,
        status: String?,
        sortBy: String
    ): List<Anime> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        val hasRemoteFilters = (!genre.isNullOrBlank() && genre != "All") ||
            (year != null && year > 0) ||
            (!type.isNullOrBlank() && type != "All") ||
            (!status.isNullOrBlank() && status != "All")

        if (!catalogNetworkMonitor.verifyConnectionBeforeCatalogFetch("Catalog Search API (Jikan v4 + AniList GraphQL)")) {
            return@withContext fallbackProvider.searchAnime(query, genre, year, type, status, sortBy)
        }

        val directRemoteMatches = mutableListOf<Anime>()

        if (cleanQuery.isNotBlank() || hasRemoteFilters) {
            coroutineScope {
                val jikanUrl = buildJikanSearchUrl(
                    query = cleanQuery,
                    type = type,
                    status = status,
                    sortBy = sortBy
                )
                val jikanDeferred = async { fetchFromJikanApi(jikanUrl) }
                val anilistDeferred = async {
                    searchAniListGraphQl(
                        query = cleanQuery,
                        genre = genre,
                        year = year,
                        type = type,
                        status = status,
                        sortBy = sortBy,
                        perPage = 25
                    )
                }
                val atDeferred = async {
                    if (cleanQuery.isNotBlank()) {
                        val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
                        fetchFromAnimeThemesVideoServer("https://api.animethemes.moe/search?q=$encoded&fields[search]=anime&include[anime]=animethemes.animethemeentries.videos,images&page[limit]=10")
                    } else {
                        emptyList()
                    }
                }

                val jikanResults = jikanDeferred.await()
                val anilistResults = anilistDeferred.await()
                val atResults = atDeferred.await()

                if (jikanResults.isNotEmpty()) {
                    fallbackProvider.mergeRemoteAnimeList(jikanResults)
                    directRemoteMatches.addAll(jikanResults)
                }
                if (anilistResults.isNotEmpty()) {
                    fallbackProvider.mergeRemoteAnimeList(anilistResults)
                    directRemoteMatches.addAll(anilistResults)
                }
                if (atResults.isNotEmpty()) {
                    fallbackProvider.mergeRemoteAnimeList(atResults)
                    directRemoteMatches.addAll(atResults)
                }

                if (jikanResults.isEmpty() && anilistResults.isEmpty() && atResults.isEmpty() && cleanQuery.isNotBlank()) {
                    catalogNetworkMonitor.reportCatalogFetchFailure(
                        sourceLabel = "Jikan v4 + AniList GraphQL Search (\"$cleanQuery\")",
                        errorDetail = "Remote search request failed or returned no upstream response"
                    )
                } else if (jikanResults.isNotEmpty() || anilistResults.isNotEmpty() || atResults.isNotEmpty()) {
                    catalogNetworkMonitor.reportCatalogFetchSuccess()
                }
            }
        }

        val localFiltered = fallbackProvider.searchAnime(query, genre, year, type, status, sortBy)

        // Ensure ALL anime returned directly by Jikan v4 API and AniList GraphQL API for this search
        // are included without being dropped by punctuation/accent differences, while respecting explicit filters.
        val filteredDirectMatches = directRemoteMatches.filter { anime ->
            val genreOk = genre.isNullOrBlank() || genre == "All" ||
                anime.genres.any { g -> g.equals(genre, ignoreCase = true) || g.contains(genre, ignoreCase = true) } ||
                anime.tags.any { t -> t.equals(genre, ignoreCase = true) }
            val yearOk = year == null || year <= 0 || anime.releaseYear == year
            val typeOk = type.isNullOrBlank() || type == "All" || anime.type.name.equals(type, ignoreCase = true)
            val statusOk = status.isNullOrBlank() || status == "All" || anime.status.name.equals(status, ignoreCase = true)
            genreOk && yearOk && typeOk && statusOk
        }

        val mergedResults = LinkedHashMap<String, Anime>()
        for (item in (filteredDirectMatches + localFiltered)) {
            val resolvedFromCatalog = fallbackProvider.getAnimeById(item.id) ?: item
            val dedupeKey = normalizeTitleForMatch(resolvedFromCatalog.titleEnglish)
                .ifBlank { resolvedFromCatalog.id }
            if (!mergedResults.containsKey(dedupeKey)) {
                mergedResults[dedupeKey] = resolvedFromCatalog
            }
        }
        mergedResults.values.toList()
    }

    private fun buildJikanSearchUrl(
        query: String,
        type: String?,
        status: String?,
        sortBy: String
    ): String {
        val params = mutableListOf<String>()
        if (query.isNotBlank()) {
            params.add("q=${URLEncoder.encode(query, "UTF-8")}")
        }
        params.add("limit=25")

        val jikanType = when (type?.uppercase()) {
            "TV" -> "tv"
            "MOVIE" -> "movie"
            "OVA" -> "ova"
            "ONA" -> "ona"
            "SPECIAL" -> "special"
            "MUSIC" -> "music"
            else -> null
        }
        if (jikanType != null) {
            params.add("type=$jikanType")
        }

        val jikanStatus = when (status?.uppercase()) {
            "RELEASING" -> "airing"
            "FINISHED" -> "complete"
            "NOT_YET_RELEASED" -> "upcoming"
            else -> null
        }
        if (jikanStatus != null) {
            params.add("status=$jikanStatus")
        }

        if (query.isBlank()) {
            when (sortBy.uppercase()) {
                "RATING" -> {
                    params.add("order_by=score")
                    params.add("sort=desc")
                }
                "NEWEST" -> {
                    params.add("order_by=start_date")
                    params.add("sort=desc")
                }
                "A_Z", "TITLE_AZ" -> {
                    params.add("order_by=title")
                    params.add("sort=asc")
                }
                else -> {
                    params.add("order_by=members")
                    params.add("sort=desc")
                }
            }
        }

        return "https://api.jikan.moe/v4/anime?${params.joinToString("&")}"
    }

    override suspend fun getEpisodesForAnime(animeId: String): List<Episode> = withContext(Dispatchers.IO) {
        val anime = getAnimeById(animeId)
        if (anime != null && catalogNetworkMonitor.checkNavigatorOnLine()) {
            runCatching {
                val romajiStreams = if (anime.titleRomaji.isNotBlank()) {
                    fetchAnimeThemesStorageStreams(anime.titleRomaji, anime.titleEnglish)
                } else emptyList()
                val liveThemes = romajiStreams.ifEmpty {
                    fetchAnimeThemesStorageStreams(anime.titleEnglish, anime.titleRomaji)
                }
                if (liveThemes.isNotEmpty()) {
                    fallbackProvider.registerRemoteAnimeStreams(anime.id, liveThemes)
                } else {
                    val upstreamStreams = fetchHiAnimeAniWatchUpstreamStreams(anime.titleEnglish, anime.titleRomaji)
                    if (upstreamStreams.isNotEmpty()) {
                        fallbackProvider.registerRemoteAnimeStreams(anime.id, upstreamStreams)
                    }
                }
            }
        }
        fallbackProvider.getEpisodesForAnime(animeId)
    }

    override suspend fun getRecommendations(animeId: String): List<Anime> = withContext(Dispatchers.IO) {
        fallbackProvider.getRecommendations(animeId)
    }

    override suspend fun getAllGenres(): List<String> = withContext(Dispatchers.IO) {
        fallbackProvider.getAllGenres()
    }

    override suspend fun getAllStudios(): List<String> = withContext(Dispatchers.IO) {
        fallbackProvider.getAllStudios()
    }

    /**
     * Fetches real anime metadata AND real 1080p .webm video streams directly from AnimeThemes API Server.
     */
    private fun fetchFromAnimeThemesVideoServer(url: String): List<Anime> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build()

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val animeArray = root.optJSONArray("anime")
                    ?: root.optJSONObject("search")?.optJSONArray("anime")
                    ?: return emptyList()
                val results = mutableListOf<Anime>()

                for (i in 0 until animeArray.length()) {
                    val item = animeArray.optJSONObject(i) ?: continue
                    val idInt = item.optInt("id", 0)
                    if (idInt == 0) continue
                    val animeId = "at_$idInt"
                    val name = item.optString("name", "").takeIf { it.isNotBlank() } ?: continue
                    val slug = item.optString("slug", name.lowercase().replace(Regex("[^a-z0-9]+"), "-"))
                    val year = item.optInt("year", 2024).let { if (it <= 0) 2024 else it }
                    val seasonStr = item.optString("season", "Winter") + " $year"
                    val synopsis = item.optString("synopsis", "").replace(Regex("<[^>]*>"), "").ifBlank {
                        "Streaming in 1080p HD directly from AnimeThemes Video Storage Server."
                    }

                    // Extract cover image
                    var posterUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg"
                    val imagesArr = item.optJSONArray("images")
                    if (imagesArr != null && imagesArr.length() > 0) {
                        for (imgIdx in 0 until imagesArr.length()) {
                            val imgObj = imagesArr.optJSONObject(imgIdx) ?: continue
                            val link = imgObj.optString("link", "")
                            if (link.startsWith("http")) {
                                posterUrl = link
                                break
                            }
                        }
                    }

                    // Extract real playable .webm video streams
                    val themesArr = item.optJSONArray("animethemes")
                    val extractedSources = mutableListOf<EpisodeSource>()
                    if (themesArr != null) {
                        for (t in 0 until themesArr.length()) {
                            val themeObj = themesArr.optJSONObject(t) ?: continue
                            val themeSlug = themeObj.optString("slug", "OP1")
                            val entriesArr = themeObj.optJSONArray("animethemeentries") ?: continue
                            for (e in 0 until entriesArr.length()) {
                                val videosArr = entriesArr.optJSONObject(e)?.optJSONArray("videos") ?: continue
                                for (v in 0 until videosArr.length()) {
                                    val videoObj = videosArr.optJSONObject(v) ?: continue
                                    val videoLink = videoObj.optString("link", "")
                                    val res = videoObj.optInt("resolution", 1080)
                                    if (videoLink.startsWith("http")) {
                                        extractedSources.add(
                                            EpisodeSource(
                                                id = "at_srv_${idInt}_${themeSlug}_$v",
                                                quality = "${res}p HD • AnimeThemes ($themeSlug)",
                                                streamUrl = videoLink,
                                                isHls = videoLink.endsWith(".m3u8"),
                                                cdnNode = "AnimeThemes Real Video Server"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (extractedSources.isNotEmpty()) {
                        val firstStream = extractedSources.first().streamUrl
                        val lastStream = extractedSources.last().streamUrl
                        val dedicatedAtSources = listOf(
                            EpisodeSource(
                                id = "hd1_hls_at_$idInt",
                                quality = "1080p HD-1 • VidStreaming (SUB)",
                                streamUrl = firstStream,
                                isHls = firstStream.endsWith(".m3u8"),
                                cdnNode = "HD-1 (VidStreaming)"
                            ),
                            EpisodeSource(
                                id = "hd2_mp4_at_$idInt",
                                quality = "1080p HD-2 • MegaCloud (SUB)",
                                streamUrl = lastStream,
                                isHls = lastStream.endsWith(".m3u8"),
                                cdnNode = "HD-2 (MegaCloud)"
                            )
                        )
                        val combinedAtSources = (extractedSources + dedicatedAtSources).distinctBy { it.id }
                        fallbackProvider.registerRemoteAnimeStreams(animeId, combinedAtSources)
                        results.add(
                            Anime(
                                id = animeId,
                                slug = slug,
                                titleEnglish = name,
                                titleRomaji = name,
                                titleJapanese = "",
                                description = synopsis,
                                posterUrl = posterUrl,
                                bannerUrl = posterUrl,
                                rating = 4.8f,
                                score = 90,
                                type = AnimeType.TV,
                                status = AnimeStatus.RELEASING,
                                episodesCount = extractedSources.size.coerceAtLeast(12),
                                releaseYear = year,
                                season = seasonStr,
                                durationMinutes = 24,
                                studio = "AnimeThemes HD",
                                genres = listOf("Action", "Fantasy"),
                                trailerUrl = "",
                                isFeatured = true,
                                isTrending = true,
                                isPopular = true
                            )
                        )
                    }
                }
                results
            }
        } catch (e: Exception) {
            Log.w(tag, "AnimeThemes server fetch warning: ${e.message}")
            emptyList()
        }
    }

    private fun parseJikanAnimeItem(item: JSONObject, index: Int = 0): Anime? {
        val malId = item.optInt("mal_id", 0)
        if (malId == 0) return null

        val titleDefault = item.optString("title").takeIf { it.isNotBlank() && it != "null" } ?: "Unknown Anime"
        val titleEn = item.optString("title_english").takeIf { it.isNotBlank() && it != "null" } ?: titleDefault
        val titleRomaji = titleDefault
        val titleJp = item.optString("title_japanese").takeIf { it != "null" } ?: ""
        val synopsis = item.optString("synopsis", "").takeIf { it != "null" } ?: ""

        val imagesJpg = item.optJSONObject("images")?.optJSONObject("jpg")
        val imagesWebp = item.optJSONObject("images")?.optJSONObject("webp")
        val posterUrl = imagesJpg?.optString("large_image_url")?.takeIf { it.isNotBlank() && it != "null" }
            ?: imagesWebp?.optString("large_image_url")?.takeIf { it.isNotBlank() && it != "null" }
            ?: imagesJpg?.optString("image_url").orEmpty()

        val trailerObj = item.optJSONObject("trailer")
        val bannerUrl = trailerObj?.optJSONObject("images")?.optString("maximum_image_url")
            ?.takeIf { it.isNotBlank() && it != "null" } ?: posterUrl

        val rawScore = item.optDouble("score", 8.4)
        val validScore = if (rawScore.isNaN() || rawScore <= 0.0) 8.4 else rawScore
        val rating5 = (validScore / 2.0).toFloat().coerceIn(1f, 5f)
        val score100 = (validScore * 10).toInt().coerceIn(1, 100)
        val episodesCount = item.optInt("episodes", 12).let { if (it <= 0) 12 else it }

        val topYear = item.optInt("year", 0)
        val airedYear = item.optJSONObject("aired")
            ?.optJSONObject("prop")
            ?.optJSONObject("from")
            ?.optInt("year", 0) ?: 0
        val year = when {
            topYear > 1950 -> topYear
            airedYear > 1950 -> airedYear
            else -> 2024
        }

        val rawSeason = item.optString("season").takeIf { it.isNotBlank() && it != "null" }
            ?.replaceFirstChar { it.uppercase() } ?: "Winter"

        val studiosArr = item.optJSONArray("studios")
        val studioName = if (studiosArr != null && studiosArr.length() > 0) {
            studiosArr.optJSONObject(0)?.optString("name", "Anime Studio") ?: "Anime Studio"
        } else "Anime Studio"

        val producersArr = item.optJSONArray("producers")
        val producers = mutableListOf<String>()
        if (producersArr != null) {
            for (p in 0 until producersArr.length()) {
                producersArr.optJSONObject(p)?.optString("name")?.takeIf { it.isNotBlank() }?.let { producers.add(it) }
            }
        }

        val genres = mutableListOf<String>()
        listOf("genres", "explicit_genres", "themes", "demographics").forEach { fieldName ->
            val arr = item.optJSONArray(fieldName)
            if (arr != null) {
                for (g in 0 until arr.length()) {
                    arr.optJSONObject(g)?.optString("name")?.takeIf { it.isNotBlank() }?.let { gName ->
                        if (gName !in genres) genres.add(gName)
                    }
                }
            }
        }
        if (genres.isEmpty()) genres.add("Action")

        val synonymTags = mutableListOf<String>()
        val synonymsArr = item.optJSONArray("title_synonyms")
        if (synonymsArr != null) {
            for (s in 0 until synonymsArr.length()) {
                synonymsArr.optString(s)?.takeIf { it.isNotBlank() && it != "null" }?.let { synonymTags.add(it) }
            }
        }
        val titlesArr = item.optJSONArray("titles")
        if (titlesArr != null) {
            for (t in 0 until titlesArr.length()) {
                titlesArr.optJSONObject(t)?.optString("title")?.takeIf { it.isNotBlank() && it != "null" }?.let { altTitle ->
                    if (altTitle !in synonymTags) synonymTags.add(altTitle)
                }
            }
        }

        val animeType = AnimeType.fromApiString(item.optString("type"))
        val animeStatus = AnimeStatus.fromApiString(
            raw = item.optString("status"),
            isAiring = item.optBoolean("airing", false)
        )

        return Anime(
            id = "mal_$malId",
            slug = titleEn.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
            titleEnglish = titleEn,
            titleRomaji = titleRomaji,
            titleJapanese = titleJp,
            description = synopsis.ifBlank { "$titleEn ($titleRomaji) — ${animeType.displayName} • Studio: $studioName." },
            posterUrl = posterUrl,
            bannerUrl = bannerUrl,
            rating = rating5,
            score = score100,
            type = animeType,
            status = animeStatus,
            episodesCount = episodesCount,
            releaseYear = year,
            season = "$rawSeason $year",
            durationMinutes = if (animeType == AnimeType.MOVIE) 110 else 24,
            studio = studioName,
            producers = producers,
            genres = genres,
            tags = synonymTags,
            trailerUrl = "",
            isFeatured = index < 3,
            isTrending = true,
            isPopular = true,
            isSeasonal = animeStatus == AnimeStatus.RELEASING
        )
    }

    /**
     * Fetches real anime metadata across all anime types (TV, Movie, OVA, ONA, Special, Music) from Jikan v4 Free API.
     */
    private fun fetchFromJikanApi(url: String): List<Anime> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build()

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val dataArray = root.optJSONArray("data") ?: return emptyList()
                val list = mutableListOf<Anime>()

                for (i in 0 until dataArray.length()) {
                    val item = dataArray.optJSONObject(i) ?: continue
                    parseJikanAnimeItem(item, i)?.let { list.add(it) }
                }
                list
            }
        } catch (e: Exception) {
            Log.w(tag, "Jikan API fetch warning: ${e.message}")
            emptyList()
        }
    }

    private fun fetchSingleJikanAnimeById(malId: Int): List<Anime> {
        return try {
            val request = Request.Builder()
                .url("https://api.jikan.moe/v4/anime/$malId")
                .header("Accept", "application/json")
                .get()
                .build()
            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val dataObj = JSONObject(body).optJSONObject("data") ?: return emptyList()
                listOfNotNull(parseJikanAnimeItem(dataObj, 0))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseAniListMediaObject(m: JSONObject): Anime? {
        val id = m.optInt("id", 0)
        if (id == 0) return null
        val idMal = m.optInt("idMal", 0)
        val resolvedId = if (idMal > 0) "mal_$idMal" else "anilist_$id"

        val titleObj = m.optJSONObject("title")
        val romaji = titleObj?.optString("romaji")?.takeIf { it.isNotBlank() && it != "null" }
            ?: titleObj?.optString("userPreferred")?.takeIf { it.isNotBlank() && it != "null" }
            ?: "Anime"
        val en = titleObj?.optString("english")?.takeIf { it.isNotBlank() && it != "null" }
            ?: romaji
        val native = titleObj?.optString("native")?.takeIf { it != "null" } ?: ""

        val coverObj = m.optJSONObject("coverImage")
        val cover = coverObj?.optString("extraLarge")?.takeIf { it.isNotBlank() && it != "null" }
            ?: coverObj?.optString("large")?.takeIf { it.isNotBlank() && it != "null" }
            ?: ""
        val banner = m.optString("bannerImage").takeIf { it.isNotBlank() && it != "null" } ?: cover

        val avgScore = m.optInt("averageScore", 0).let {
            if (it > 0) it else m.optInt("meanScore", 84).let { ms -> if (ms > 0) ms else 84 }
        }
        val seasonYear = m.optInt("seasonYear", 0)
        val startYear = m.optJSONObject("startDate")?.optInt("year", 0) ?: 0
        val yearVal = when {
            seasonYear > 1950 -> seasonYear
            startYear > 1950 -> startYear
            else -> 2024
        }
        val rawSeason = m.optString("season").takeIf { it.isNotBlank() && it != "null" }
            ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Winter"

        val genresArr = m.optJSONArray("genres") ?: JSONArray()
        val genresList = mutableListOf<String>()
        for (g in 0 until genresArr.length()) {
            genresArr.optString(g)?.takeIf { it.isNotBlank() && it != "null" }?.let { genresList.add(it) }
        }

        val tagsList = mutableListOf<String>()
        val synonymsArr = m.optJSONArray("synonyms")
        if (synonymsArr != null) {
            for (s in 0 until synonymsArr.length()) {
                synonymsArr.optString(s)?.takeIf { it.isNotBlank() && it != "null" }?.let { tagsList.add(it) }
            }
        }
        val mediaTagsArr = m.optJSONArray("tags")
        if (mediaTagsArr != null) {
            for (t in 0 until mediaTagsArr.length().coerceAtMost(10)) {
                val tagName = mediaTagsArr.optJSONObject(t)?.optString("name")?.takeIf { it.isNotBlank() && it != "null" }
                if (tagName != null && tagName !in tagsList) {
                    tagsList.add(tagName)
                    if (tagName.equals("Isekai", ignoreCase = true) && "Isekai" !in genresList) {
                        genresList.add("Isekai")
                    }
                }
            }
        }
        if (genresList.isEmpty()) genresList.add("Action")

        val studiosNodes = m.optJSONObject("studios")?.optJSONArray("nodes")
        val studioName = if (studiosNodes != null && studiosNodes.length() > 0) {
            studiosNodes.optJSONObject(0)?.optString("name")?.takeIf { it.isNotBlank() } ?: "Anime Studio"
        } else "Anime Studio"

        val charactersList = mutableListOf<AnimeCharacter>()
        val charEdges = m.optJSONObject("characters")?.optJSONArray("edges")
        if (charEdges != null) {
            for (c in 0 until charEdges.length()) {
                val edge = charEdges.optJSONObject(c) ?: continue
                val role = edge.optString("role", "MAIN").lowercase().replaceFirstChar { it.uppercase() }
                val node = edge.optJSONObject("node") ?: continue
                val charName = node.optJSONObject("name")?.optString("full")?.takeIf { it.isNotBlank() } ?: continue
                val charImg = node.optJSONObject("image")?.optString("large").orEmpty()
                val vaArr = edge.optJSONArray("voiceActors")
                val vaName = if (vaArr != null && vaArr.length() > 0) {
                    vaArr.optJSONObject(0)?.optJSONObject("name")?.optString("full").orEmpty()
                } else ""
                charactersList.add(AnimeCharacter(name = charName, role = role, avatarUrl = charImg, voiceActor = vaName))
            }
        }

        val animeType = AnimeType.fromApiString(m.optString("format"))
        val animeStatus = AnimeStatus.fromApiString(m.optString("status"))
        val epCount = m.optInt("episodes", 12).let { if (it <= 0) (if (animeType == AnimeType.MOVIE) 1 else 12) else it }
        val durationMin = m.optInt("duration", 24).let { if (it <= 0) (if (animeType == AnimeType.MOVIE) 110 else 24) else it }
        val cleanDesc = m.optString("description", "").takeIf { it != "null" }
            ?.replace(Regex("<[^>]*>"), "")
            ?.trim()
            .orEmpty()
            .ifBlank { "$en ($romaji) — ${animeType.displayName} • Studio: $studioName." }

        return Anime(
            id = resolvedId,
            slug = en.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
            titleEnglish = en,
            titleRomaji = romaji,
            titleJapanese = native,
            description = cleanDesc,
            posterUrl = cover,
            bannerUrl = banner,
            rating = (avgScore / 20f).coerceIn(1f, 5f),
            score = avgScore.coerceIn(1, 100),
            type = animeType,
            status = animeStatus,
            episodesCount = epCount,
            releaseYear = yearVal,
            season = "$rawSeason $yearVal",
            durationMinutes = durationMin,
            studio = studioName,
            genres = genresList,
            tags = tagsList,
            trailerUrl = "",
            characters = charactersList,
            isTrending = true,
            isPopular = true,
            isSeasonal = animeStatus == AnimeStatus.RELEASING
        )
    }

    /**
     * Fetches anime from AniList Official GraphQL API (https://graphql.anilist.co) with full search & filter variables.
     */
    private fun fetchFromAniListGraphQl(
        sort: String = "TRENDING_DESC",
        perPage: Int = 20
    ): List<Anime> {
        return searchAniListGraphQl(
            query = "",
            genre = null,
            year = null,
            type = null,
            status = null,
            sortBy = sort,
            perPage = perPage
        )
    }

    private fun searchAniListGraphQl(
        query: String,
        genre: String?,
        year: Int?,
        type: String?,
        status: String?,
        sortBy: String,
        perPage: Int = 25
    ): List<Anime> {
        return try {
            val gqlQuery = """
                query (
                  ${'$'}page: Int,
                  ${'$'}perPage: Int,
                  ${'$'}search: String,
                  ${'$'}format: MediaFormat,
                  ${'$'}status: MediaStatus,
                  ${'$'}genre: String,
                  ${'$'}seasonYear: Int,
                  ${'$'}sort: [MediaSort]
                ) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(
                      type: ANIME,
                      isAdult: false,
                      search: ${'$'}search,
                      format: ${'$'}format,
                      status: ${'$'}status,
                      genre: ${'$'}genre,
                      seasonYear: ${'$'}seasonYear,
                      sort: ${'$'}sort
                    ) {
                      id
                      idMal
                      format
                      status
                      title { romaji english native userPreferred }
                      synonyms
                      description(asHtml: false)
                      episodes
                      duration
                      season
                      seasonYear
                      startDate { year month }
                      averageScore
                      meanScore
                      popularity
                      coverImage { extraLarge large }
                      bannerImage
                      genres
                      tags { name rank }
                      studios(isMain: true) { nodes { name } }
                      characters(sort: [ROLE, RELEVANCE], perPage: 6) {
                        edges {
                          role
                          node { name { full } image { large } }
                          voiceActors(language: JAPANESE, sort: [RELEVANCE]) { name { full } }
                        }
                      }
                    }
                  }
                }
            """.trimIndent()

            val variables = JSONObject().apply {
                put("page", 1)
                put("perPage", perPage.coerceIn(1, 50))
                if (query.isNotBlank()) {
                    put("search", query.trim())
                }
                val anilistFormat = when (type?.uppercase()) {
                    "TV" -> "TV"
                    "MOVIE" -> "MOVIE"
                    "OVA" -> "OVA"
                    "ONA" -> "ONA"
                    "SPECIAL" -> "SPECIAL"
                    "MUSIC" -> "MUSIC"
                    else -> null
                }
                if (anilistFormat != null) {
                    put("format", anilistFormat)
                }
                val anilistStatus = when (status?.uppercase()) {
                    "RELEASING" -> "RELEASING"
                    "FINISHED" -> "FINISHED"
                    "NOT_YET_RELEASED" -> "NOT_YET_RELEASED"
                    else -> null
                }
                if (anilistStatus != null) {
                    put("status", anilistStatus)
                }
                val supportedAniListGenres = setOf(
                    "Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", "Horror",
                    "Mahou Shoujo", "Mecha", "Music", "Mystery", "Psychological", "Romance",
                    "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller"
                )
                if (!genre.isNullOrBlank() && genre != "All" && supportedAniListGenres.any { it.equals(genre, ignoreCase = true) }) {
                    put("genre", supportedAniListGenres.first { it.equals(genre, ignoreCase = true) })
                }
                if (year != null && year > 1950) {
                    put("seasonYear", year)
                }
                val sortEnum = when (sortBy.uppercase()) {
                    "TRENDING_DESC" -> "TRENDING_DESC"
                    "POPULARITY_DESC", "POPULARITY" -> if (query.isNotBlank()) "SEARCH_MATCH" else "POPULARITY_DESC"
                    "SCORE_DESC", "RATING" -> "SCORE_DESC"
                    "NEWEST" -> "START_DATE_DESC"
                    "A_Z", "TITLE_AZ" -> "TITLE_ENGLISH"
                    else -> if (query.isNotBlank()) "SEARCH_MATCH" else "POPULARITY_DESC"
                }
                put("sort", JSONArray().put(sortEnum))
            }

            val payload = JSONObject()
                .put("query", gqlQuery)
                .put("variables", variables)
                .toString()

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .header("Accept", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val mediaArr = JSONObject(body)
                    .optJSONObject("data")
                    ?.optJSONObject("Page")
                    ?.optJSONArray("media") ?: return emptyList()

                val list = mutableListOf<Anime>()
                for (i in 0 until mediaArr.length()) {
                    val m = mediaArr.optJSONObject(i) ?: continue
                    parseAniListMediaObject(m)?.let { list.add(it) }
                }
                list
            }
        } catch (e: Exception) {
            Log.w(tag, "AniList GraphQL fetch warning: ${e.message}")
            emptyList()
        }
    }

    private fun fetchSingleAniListAnimeById(aniId: Int): List<Anime> {
        return try {
            val gqlQuery = """
                query (${'$'}id: Int) {
                  Media(id: ${'$'}id, type: ANIME) {
                    id
                    idMal
                    format
                    status
                    title { romaji english native userPreferred }
                    synonyms
                    description(asHtml: false)
                    episodes
                    duration
                    season
                    seasonYear
                    startDate { year month }
                    averageScore
                    meanScore
                    coverImage { extraLarge large }
                    bannerImage
                    genres
                    tags { name rank }
                    studios(isMain: true) { nodes { name } }
                  }
                }
            """.trimIndent()
            val payload = JSONObject()
                .put("query", gqlQuery)
                .put("variables", JSONObject().put("id", aniId))
                .toString()
            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()
            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val mediaObj = JSONObject(body).optJSONObject("data")?.optJSONObject("Media") ?: return emptyList()
                listOfNotNull(parseAniListMediaObject(mediaObj))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun titlesMatchStrictly(candidateName: String, targetPrimary: String, targetSecondary: String = ""): Boolean {
        val cand = normalizeTitleForMatch(candidateName)
        if (cand.isBlank()) return false
        val p1 = normalizeTitleForMatch(targetPrimary)
        val p2 = normalizeTitleForMatch(targetSecondary)
        return (p1.isNotBlank() && (cand == p1 || cand.contains(p1) || p1.contains(cand))) ||
            (p2.isNotBlank() && (cand == p2 || cand.contains(p2) || p2.contains(cand)))
    }

    /**
     * Fetches real direct .webm video streams from AnimeThemes Free Video Storage Server
     * using the /search endpoint ONLY if the returned anime strictly matches the requested anime title.
     */
    private fun fetchAnimeThemesStorageStreams(animeTitle: String, altTitle: String = ""): List<EpisodeSource> {
        return try {
            val cleanQuery = animeTitle.trim()
            if (cleanQuery.isBlank()) return emptyList()
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val url = "https://api.animethemes.moe/search?q=$encoded&fields[search]=anime&include[anime]=animethemes.animethemeentries.videos&page[limit]=4"
            val request = Request.Builder().url(url).header("Accept", "application/json").get().build()

            RetrofitClient.okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val root = JSONObject(body)
                val animeArr = root.optJSONObject("search")?.optJSONArray("anime")
                    ?: root.optJSONArray("anime")
                    ?: return emptyList()
                if (animeArr.length() == 0) return emptyList()

                var matchedAnimeObj: JSONObject? = null
                for (i in 0 until animeArr.length()) {
                    val candidate = animeArr.optJSONObject(i) ?: continue
                    val candidateName = candidate.optString("name", "")
                    val candidateSlug = candidate.optString("slug", "")
                    if (titlesMatchStrictly(candidateName, animeTitle, altTitle) ||
                        titlesMatchStrictly(candidateSlug, animeTitle, altTitle)
                    ) {
                        matchedAnimeObj = candidate
                        break
                    }
                }
                val targetAnime = matchedAnimeObj ?: return emptyList()
                val themes = targetAnime.optJSONArray("animethemes") ?: return emptyList()
                val sources = mutableListOf<EpisodeSource>()

                for (t in 0 until themes.length().coerceAtMost(4)) {
                    val theme = themes.optJSONObject(t) ?: continue
                    val slug = theme.optString("slug", "OP")
                    val entries = theme.optJSONArray("animethemeentries") ?: continue
                    for (e in 0 until entries.length()) {
                        val videos = entries.optJSONObject(e)?.optJSONArray("videos") ?: continue
                        for (v in 0 until videos.length()) {
                            val vObj = videos.optJSONObject(v) ?: continue
                            val link = vObj.optString("link").takeIf { it.isNotBlank() } ?: continue
                            if (link.contains("BungakuShoujo", ignoreCase = true) && !cleanQuery.contains("bungaku", ignoreCase = true)) {
                                continue
                            }
                            val res = vObj.optInt("resolution", 1080)
                            sources.add(
                                EpisodeSource(
                                    id = "at_live_${slug}_$v",
                                    quality = "${res}p AnimeThemes Storage ($slug)",
                                    streamUrl = link,
                                    isHls = link.endsWith(".m3u8"),
                                    cdnNode = "AnimeThemes Free Storage Server"
                                )
                            )
                        }
                    }
                }
                sources
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Queries HiAnime / AniWatch / Consumet Zoro upstream API endpoints to resolve
     * multi-server HLS (.m3u8) streams ONLY if the matched anime strictly matches the requested anime title.
     */
    private fun fetchHiAnimeAniWatchUpstreamStreams(animeTitle: String, altTitle: String = ""): List<EpisodeSource> {
        return try {
            val encoded = URLEncoder.encode(animeTitle, "UTF-8")
            val activeBase = RetrofitClient.getActiveBaseUrl().trimEnd('/')
            val searchBase = if (activeBase.contains("consumet") || activeBase.contains("zoro") || activeBase.contains("aniwatch") || activeBase.contains("hianime")) {
                activeBase
            } else {
                "https://api.consumet.org/anime/zoro"
            }

            val searchReq = Request.Builder().url("$searchBase/$encoded").get().build()
            val zoroAnimeId = RetrofitClient.okHttpClient.newCall(searchReq).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string() ?: return emptyList()
                val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
                if (results.length() == 0) return emptyList()
                var matchedId = ""
                for (i in 0 until results.length()) {
                    val obj = results.optJSONObject(i) ?: continue
                    val resTitle = obj.optString("title", "")
                    if (titlesMatchStrictly(resTitle, animeTitle, altTitle)) {
                        matchedId = obj.optString("id").orEmpty()
                        break
                    }
                }
                matchedId
            }
            if (zoroAnimeId.isBlank()) return emptyList()

            val infoReq = Request.Builder().url("$searchBase/info?id=$zoroAnimeId").get().build()
            val firstEpId = RetrofitClient.okHttpClient.newCall(infoReq).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string() ?: return emptyList()
                val eps = JSONObject(body).optJSONArray("episodes") ?: return emptyList()
                if (eps.length() == 0) return emptyList()
                eps.optJSONObject(0)?.optString("id").orEmpty()
            }
            if (firstEpId.isBlank()) return emptyList()

            val resolvedSources = mutableListOf<EpisodeSource>()
            val upstreamServers = listOf(
                "vidstreaming" to "HD-1 (VidStreaming • HiAnime)",
                "megacloud" to "HD-2 (MegaCloud • AniWatch)",
                "streamsb" to "StreamSB (HLS Backup)",
                "streamtape" to "StreamTape (Direct Cloud)"
            )

            for ((serverParam, serverLabel) in upstreamServers.take(2)) {
                runCatching {
                    val watchUrl = "$searchBase/watch?episodeId=$firstEpId&server=$serverParam"
                    val watchReq = Request.Builder().url(watchUrl).get().build()
                    RetrofitClient.okHttpClient.newCall(watchReq).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string().orEmpty()
                            val sourcesArr = JSONObject(body).optJSONArray("sources")
                            if (sourcesArr != null) {
                                for (i in 0 until sourcesArr.length()) {
                                    val srcObj = sourcesArr.optJSONObject(i) ?: continue
                                    val streamUrl = srcObj.optString("url").takeIf { it.isNotBlank() } ?: continue
                                    val quality = srcObj.optString("quality", "1080p")
                                    val isM3u8 = srcObj.optBoolean("isM3U8", streamUrl.contains(".m3u8"))
                                    resolvedSources.add(
                                        EpisodeSource(
                                            id = "upstream_${serverParam}_$i",
                                            quality = "$quality • $serverLabel",
                                            streamUrl = streamUrl,
                                            isHls = isM3u8,
                                            cdnNode = serverLabel
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            resolvedSources
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun authorizeStream(
        episodeId: String,
        animeId: String,
        preferredQuality: String = "1080p",
        authToken: String? = null
    ): PlaybackSessionResponse = withContext(Dispatchers.IO) {
        try {
            val request = StreamAuthorizationRequest(
                episodeId = episodeId,
                animeId = animeId,
                clientSessionId = UUID.randomUUID().toString(),
                preferredQuality = preferredQuality
            )
            val response = apiService.authorizePlaybackSession(
                authToken = authToken?.let { "Bearer $it" },
                request = request
            )
            if (response.success && response.data != null) {
                return@withContext response.data
            }
        } catch (_: Exception) {}

        val ep = fallbackProvider.getEpisodesForAnime(animeId).find { it.id == episodeId }
        val sources = ep?.sources ?: emptyList()
        val subs = ep?.subtitles ?: emptyList()
        val auds = ep?.audioTracks ?: emptyList()

        PlaybackSessionResponse(
            sessionId = "sess_${UUID.randomUUID()}",
            episodeId = episodeId,
            expiresAt = System.currentTimeMillis() + (1000 * 60 * 60 * 4),
            masterPlaylistUrl = sources.firstOrNull()?.streamUrl ?: "",
            streamQualities = sources.map {
                com.example.data.network.model.QualityStreamDto(
                    quality = it.quality,
                    url = it.streamUrl
                )
            },
            subtitles = subs.map {
                com.example.data.network.model.SubtitleDto(
                    id = it.id,
                    language = it.language,
                    label = it.label,
                    url = it.url,
                    isDefault = it.isDefault
                )
            },
            audioTracks = auds.map {
                com.example.data.network.model.AudioTrackDto(
                    id = it.id,
                    language = it.language,
                    label = it.label,
                    isDefault = it.isDefault
                )
            },
            cdnNode = sources.firstOrNull()?.cdnNode ?: "Multi-Server Storage",
            drmToken = null
        )
    }

    private fun AnimeDto.toDomain(): Anime = Anime(
        id = id,
        slug = slug,
        titleEnglish = titleEnglish,
        titleRomaji = titleRomaji ?: titleEnglish,
        titleJapanese = titleJapanese ?: "",
        description = description,
        posterUrl = posterUrl,
        bannerUrl = bannerUrl ?: posterUrl,
        rating = rating,
        score = score ?: (rating * 20).toInt(),
        type = runCatching { AnimeType.valueOf(type) }.getOrDefault(AnimeType.TV),
        status = runCatching { AnimeStatus.valueOf(status) }.getOrDefault(AnimeStatus.FINISHED),
        episodesCount = episodesCount,
        releaseYear = releaseYear,
        season = season ?: "Winter 2026",
        durationMinutes = durationMinutes,
        studio = studio,
        producers = producers ?: emptyList(),
        genres = genres,
        tags = tags ?: emptyList(),
        trailerUrl = trailerUrl ?: "",
        characters = characters?.map {
            AnimeCharacter(
                name = it.name,
                role = it.role,
                avatarUrl = it.avatarUrl,
                voiceActor = it.voiceActor ?: ""
            )
        } ?: emptyList(),
        isFeatured = isFeatured,
        isTrending = isTrending,
        isPopular = isPopular,
        isSeasonal = isSeasonal,
        nextEpisodeAirDate = nextEpisodeAirDate
    )

    private fun EpisodeDto.toDomain(fallbackEp: Episode? = null): Episode {
        return Episode(
            id = id,
            animeId = animeId,
            episodeNumber = episodeNumber,
            title = title,
            thumbnail = thumbnail,
            durationSeconds = durationSeconds,
            airDate = airDate,
            introStartSec = introStartSec,
            introEndSec = introEndSec,
            outroStartSec = outroStartSec,
            outroEndSec = outroEndSec,
            synopsis = synopsis ?: "",
            sources = fallbackEp?.sources ?: emptyList(),
            subtitles = fallbackEp?.subtitles ?: emptyList(),
            audioTracks = fallbackEp?.audioTracks ?: emptyList()
        )
    }
}
