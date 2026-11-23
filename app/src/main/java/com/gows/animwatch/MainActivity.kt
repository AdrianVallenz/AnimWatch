package com.gows.animwatch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.gows.animwatch.data.remote.GoogleAuthDeepLinkBus
import com.gows.animwatch.ui.AnimWatchApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GoogleAuthDeepLinkBus.dispatchFromIntent(intent)

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
        hideSystemNavigationBar()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigationBar()
        }
    }

    private fun hideSystemNavigationBar() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        GoogleAuthDeepLinkBus.dispatchFromIntent(intent)
    }
}
