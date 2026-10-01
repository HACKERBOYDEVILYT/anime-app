package com.example.data.model

data class ApiConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val category: String = "Streaming HLS", // "Streaming HLS", "Catalog REST", "Metadata AniList", "Backup Mirror"
    val apiKey: String? = null,
    val isActive: Boolean = false,
    val status: String = "Online",           // "Online", "Degraded", "Offline", "Testing"
    val latencyMs: Long = 45L,
    val lastTested: String = "Just now"
)
