package com.gows.animwatch.data.local

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gows.animwatch.MainActivity
import com.gows.animwatch.R
import com.gows.animwatch.data.model.NotificationItem

object AppNotificationHelper {

    private const val CHANNEL_ID = "animwatch_episode_updates"
    private const val CHANNEL_NAME = "Update Episode Anime"
    private const val NOTIFICATION_ID = 4108

    fun postEpisodeUpdate(
        context: Context,
        notifications: List<NotificationItem>
    ) {
        val unread = notifications.filter { !it.isRead }
        val top = unread.firstOrNull() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        runCatching {
            ensureChannel(context)
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(top.title)
                .setContentText(top.message)
                .setStyle(
                    if (unread.size > 1) {
                        NotificationCompat.InboxStyle()
                            .setBigContentTitle("${unread.size} Update Anime Terbaru")
                            .also { style ->
                                unread.take(4).forEach { item ->
                                    style.addLine("${item.title} — ${item.message}")
                                }
                            }
                    } else {
                        NotificationCompat.BigTextStyle().bigText(top.message)
                    }
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            manager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun ensureChannel(context: Context) {
        val sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        if (sysManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi rilis episode anime terbaru & jadwal tayang anime favorit"
            }
            sysManager.createNotificationChannel(channel)
        }
    }
}
