package com.wavora.app.core.performance

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.wavora.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilImageCacheConfig @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun build(): ImageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.15)       // 15% of app heap
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(File(context.cacheDir, "image_cache"))
                .maxSizeBytes(100L * 1024 * 1024)   // 100 MB
                .build()
        }
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        // INEXACT: reuse larger cached bitmaps at smaller request sizes
        // Saves a full decode every time we load album art at 48dp after
        // having loaded it at 800px for NowPlaying
        .allowRgb565(true)           // use RGB_565 for opaque bitmaps — 50% less RAM
        .crossfade(false)            // disabled globally; enabled selectively per call
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()

}