# ==============================================================================
# KuroStream Android Production R8 & ProGuard Hardening Configuration
# ==============================================================================

# 1. General Code Obfuscation & Shrinking
-repackageclasses 'com.example.internal'
-allowaccessmodification
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 2. Aggressive Anti-Reverse Engineering & Debug Stripping
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
-assumenosideeffects class okhttp3.logging.HttpLoggingInterceptor {
    public void setLevel(...);
}

# 3. Preserve Moshi JSON Serialization Models (Avoid Reflection Breakage)
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**

# 4. Preserve Retrofit & OkHttp Interfaces & Endpoints
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# 5. Preserve Room Database Entities and DAOs
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <methods>;
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-dontwarn androidx.room.**

# 6. Preserve Jetpack Compose & Kotlin Coroutines
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView { *; }
-dontwarn androidx.compose.**
-dontwarn kotlinx.coroutines.**

# 7. Protect Security & Cryptographic Handlers
-keep class com.example.security.** { *; }

# 8. Preserve WebView JavascriptInterface Bridge & Media3 ExoPlayer
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

