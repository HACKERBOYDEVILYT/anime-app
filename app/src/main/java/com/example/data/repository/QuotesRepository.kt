package com.example.data.repository

data class AnimeQuote(
    val id: String,
    val character: String,
    val anime: String,
    val quoteEnglish: String,
    val quoteBangla: String,
    val characterAvatarUrl: String,
    val bgImageUrl: String,
    val likesCount: Int = 142
)

class QuotesRepository {
    private val quotes = listOf(
        AnimeQuote(
            id = "q_1",
            character = "Frieren",
            anime = "Frieren: Beyond Journey's End",
            quoteEnglish = "It's the little memories from everyday life that matter the most in the end.",
            quoteBangla = "দিনের শেষে প্রাত্যহিক জীবনের ছোট ছোট স্মৃতিগুলোই সবচেয়ে বেশি মূল্যবান হয়ে থাকে।",
            characterAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            bgImageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
            likesCount = 582
        ),
        AnimeQuote(
            id = "q_2",
            character = "Satoru Gojo",
            anime = "Jujutsu Kaisen",
            quoteEnglish = "Don't worry, I'm the strongest. Throughout Heaven and Earth, I alone am the honored one.",
            quoteBangla = "চিন্তার কিছু নেই, আমিই সর্বশ্রেষ্ঠ। স্বর্গ আর মর্ত্যের মাঝে আমি একাই সম্মানিত।",
            characterAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            bgImageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800",
            likesCount = 1290
        ),
        AnimeQuote(
            id = "q_3",
            character = "Sung Jin-Woo",
            anime = "Solo Leveling",
            quoteEnglish = "If I don't move forward, no one will protect what matters to me. Arise.",
            quoteBangla = "আমি যদি সামনে এগিয়ে না যাই, তবে আমার প্রিয়জনদের রক্ষা করার কেউ থাকবে না। জেগে ওঠো।",
            characterAvatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200",
            bgImageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800",
            likesCount = 940
        ),
        AnimeQuote(
            id = "q_4",
            character = "Erwin Smith",
            anime = "Attack on Titan",
            quoteEnglish = "My soldiers do not buckle or yield when faced with the cruelty of this world! My soldiers push forward!",
            quoteBangla = "এই পৃথিবীর নিষ্ঠুরতার মুখে আমার সৈনিকেরা কখনো মাথা নত করে না! আমার সৈনিকেরা এগিয়ে চলো!",
            characterAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
            bgImageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800",
            likesCount = 2100
        )
    )

    fun getDailyQuote(): AnimeQuote = quotes.first()
    fun getAllQuotes(): List<AnimeQuote> = quotes
    fun getRandomQuote(): AnimeQuote = quotes.random()
}
