package com.gows.animwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.gows.animwatch.ui.AnimWatchApp
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.ObsidianVoid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ObsidianVoid.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(ObsidianVoid.toArgb())
        )
        setContent {
            AnimWatchTheme {
                AnimWatchApp()
            }
        }
    }
}
