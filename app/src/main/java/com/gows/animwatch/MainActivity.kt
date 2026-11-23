package com.gows.animwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.gows.animwatch.ui.AnimWatchApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure lightweight, high-throughput Coil ImageLoader (RGB_565 cuts bitmap RAM in half)
        val appContext = applicationContext
        Coil.setImageLoader {
            ImageLoader.Builder(appContext)
                .allowRgb565(true)
                .respectCacheHeaders(false)
                .crossfade(160)
                .memoryCache {
                    MemoryCache.Builder(appContext)
                        .maxSizePercent(0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(appContext.cacheDir.resolve("coil_poster_cache"))
                        .maxSizeBytes(80L * 1024L * 1024L)
                        .build()
                }
                .build()
        }

        enableEdgeToEdge()
        setContent {
            AnimWatchApp()
        }
    }
}
