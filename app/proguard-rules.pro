# ── WAVORA ProGuard / R8 Rules ────────────────────────────────────────────────
# Phase 8: Full audit. R8 full-mode is enabled via gradle.properties.

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
# Coroutines debug agent — not needed in release
-dontwarn kotlinx.coroutines.debug.**

# ── Domain models & Room entities ─────────────────────────────────────────────
# Room reads entity fields via reflection. Data classes used as Room rows
# must NOT be renamed.
-keep class com.wavora.app.domain.model.** { *; }
-keep class com.wavora.app.data.local.entity.** { *; }

# Keep Room DAO interfaces — Room generates the implementation at compile time,
# but the interface methods are referenced by generated code that R8 may prune.
-keep interface com.wavora.app.data.local.dao.** { *; }

# Keep Room-generated _Impl classes (generated at compile time, not in source)
-keep class **_Impl { *; }
-keep class **_Impl$* { *; }

# ── Media3 / ExoPlayer ────────────────────────────────────────────────────────
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**
# MediaSession callback stubs used by Auto / WearOS
-keep class * extends androidx.media3.session.MediaSession$Callback { *; }

# ── Android AudioEffect (Equalizer) ──────────────────────────────────────────
-keep class android.media.audiofx.** { *; }

# ── Coil ─────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**
# Coil uses OkHttp via ServiceLoader — keep the service entries
-keep class okhttp3.internal.** { *; }
-dontwarn okhttp3.**

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Hilt Workers (AssistedInject) ─────────────────────────────────────────────
-keep class * extends androidx.hilt.work.HiltWorker { *; }
-keep @dagger.assisted.AssistedInject class * { *; }

# ── Navigation Compose ────────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ── Palette ───────────────────────────────────────────────────────────────────
-keep class androidx.palette.** { *; }

# ── Startup ───────────────────────────────────────────────────────────────────
-keep class androidx.startup.** { *; }

# ── Enum classes ─────────────────────────────────────────────────────────────
# Enums used in DataStore (stored by name) and nav args must keep their names
-keepnames enum com.wavora.app.domain.model.** { *; }
-keepnames enum com.wavora.app.ui.screens.smartplaylist.SmartPlaylistType { *; }

# ── Log stripping (release only) ─────────────────────────────────────────────
# Removes verbose/debug/info log calls. Error logs are kept for crash reports.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static boolean isLoggable(...);
}

# ── Serialisation safety ──────────────────────────────────────────────────────
# SongEntity and other Room models use @ColumnInfo — field names must survive
-keepclassmembers class com.wavora.app.data.local.entity.** {
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.Embedded <fields>;
    @androidx.room.Relation <fields>;
}

# ── Miscellaneous ──────────────────────────────────────────────────────────────
-dontwarn javax.annotation.**
-dontwarn com.google.errorprone.**
