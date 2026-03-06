# ── WAVORA ProGuard Rules ─────────────────────────────────────────────────────

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Kotlin data classes used as Room entities and domain models
-keep class com.wavora.app.domain.model.** { *; }
-keep class com.wavora.app.data.local.entity.** { *; }

# Keep Room DAOs (accessed via reflection)
-keep interface com.wavora.app.data.local.dao.** { *; }

# Keep Kotlin Coroutines (needed for debug stack traces)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Strip logging from release builds (no debug info leakage)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
