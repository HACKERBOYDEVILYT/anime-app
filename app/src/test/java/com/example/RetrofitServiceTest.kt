package com.example

import com.example.data.network.RetrofitClient
import com.example.data.network.model.ApiResponse
import com.example.data.network.model.AuthResponse
import com.example.data.network.model.LoginRequest
import com.example.data.network.model.PlaybackSessionResponse
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitServiceTest {

    @Test
    fun `test moshi serialization of playback session response`() {
        val json = """
            {
                "success": true,
                "data": {
                    "sessionId": "sess_12345",
                    "episodeId": "ep_1",
                    "expiresAt": 1750000000000,
                    "masterPlaylistUrl": "https://cdn.kurostream.app/hls/master.m3u8",
                    "streamQualities": [
                        {"quality": "1080p", "url": "https://cdn.kurostream.app/hls/1080p.m3u8"},
                        {"quality": "720p", "url": "https://cdn.kurostream.app/hls/720p.m3u8"}
                    ],
                    "subtitles": [
                        {"id": "sub_en", "language": "en", "label": "English", "url": "https://cdn.kurostream.app/subs/en.vtt", "isDefault": true}
                    ],
                    "audioTracks": [
                        {"id": "aud_ja", "language": "ja", "label": "Japanese [Original]", "isDefault": true}
                    ],
                    "cdnNode": "Cloudflare Edge Tokyo"
                }
            }
        """.trimIndent()

        val type = Types.newParameterizedType(ApiResponse::class.java, PlaybackSessionResponse::class.java)
        val adapter = RetrofitClient.moshi.adapter<ApiResponse<PlaybackSessionResponse>>(type)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertTrue(response?.success == true)
        val sessionData = response?.data
        assertNotNull(sessionData)
        assertEquals("sess_12345", sessionData?.sessionId)
        assertEquals("https://cdn.kurostream.app/hls/master.m3u8", sessionData?.masterPlaylistUrl)
        assertEquals(2, sessionData?.streamQualities?.size)
    }

    @Test
    fun `test serialization of auth models`() {
        val loginReq = LoginRequest("otaku@stream.app", "secretPass123")
        val reqJson = RetrofitClient.moshi.adapter(LoginRequest::class.java).toJson(loginReq)
        assertTrue(reqJson.contains("otaku@stream.app"))

        val authRespJson = """
            {
                "token": "jwt_token_sample",
                "user": {
                    "id": "u_1",
                    "username": "AnimeFan",
                    "email": "otaku@stream.app",
                    "avatarUrl": "https://sample.jpg",
                    "tier": "Ultra VIP",
                    "role": "USER",
                    "episodesWatched": 45,
                    "watchTimeHours": 18.2
                }
            }
        """.trimIndent()
        val authResp = RetrofitClient.moshi.adapter(AuthResponse::class.java).fromJson(authRespJson)
        assertNotNull(authResp)
        assertEquals("jwt_token_sample", authResp?.token)
        assertEquals("AnimeFan", authResp?.user?.username)
    }

    @Test
    fun `test retrofit client creation and singleton service`() {
        assertNotNull(RetrofitClient.retrofit)
        assertNotNull(RetrofitClient.apiService)
        assertNotNull(RetrofitClient.okHttpClient)
    }
}
