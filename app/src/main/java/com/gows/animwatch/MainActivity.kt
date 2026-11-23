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
import com.gows.animwatch.data.local.AppNotificationHelper
import com.gows.animwatch.data.local.NotificationDeepLinkBus
import com.gows.animwatch.data.remote.GoogleAuthDeepLinkBus
import com.gows.animwatch.ui.AnimWatchApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchDeepLinksOnce(intent)

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
        dispatchDeepLinksOnce(intent)
    }

    private fun dispatchDeepLinksOnce(incomingIntent: Intent?) {
        if (incomingIntent == null) return
        if (!incomingIntent.getBooleanExtra(EXTRA_DEEP_LINK_CONSUMED, false)) {
            GoogleAuthDeepLinkBus.dispatchFromIntent(incomingIntent)
            NotificationDeepLinkBus.dispatchFromIntent(incomingIntent)
            incomingIntent.putExtra(EXTRA_DEEP_LINK_CONSUMED, true)
            incomingIntent.data = null
            incomingIntent.removeExtra(AppNotificationHelper.EXTRA_ANIME_ID)
            incomingIntent.removeExtra(AppNotificationHelper.EXTRA_NOTIFICATION_ID)
        }
    }

    companion object {
        private const val EXTRA_DEEP_LINK_CONSUMED = "extra_animwatch_deep_link_consumed"
    }
}
