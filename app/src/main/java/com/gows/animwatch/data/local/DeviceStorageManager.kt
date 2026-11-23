package com.gows.animwatch.data.local

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import coil.Coil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gows.animwatch.data.model.Anime
import com.gows.animwatch.data.model.AnimeArtTheme
import com.gows.animwatch.data.model.DownloadStatus
import com.gows.animwatch.data.model.DownloadTask
import com.gows.animwatch.data.model.Episode
import com.gows.animwatch.data.model.SubtitleCue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

@Immutable
data class DeviceStorageInfo(
    val totalDeviceBytes: Long = 0L,
    val availableDeviceBytes: Long = 0L,
    val usedDeviceBytes: Long = 0L,
    val animWatchOfflineBytes: Long = 0L,
    val appCacheBytes: Long = 0L,
    val storagePathLabel: String = "Penyimpanan Perangkat"
) {
    val deviceUsedFraction: Float
        get() = if (totalDeviceBytes > 0L) {
            (usedDeviceBytes.toFloat() / totalDeviceBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val availableLabel: String get() = formatStorageBytes(availableDeviceBytes)
    val totalLabel: String get() = formatStorageBytes(totalDeviceBytes)
    val usedLabel: String get() = formatStorageBytes(usedDeviceBytes)
    val offlineUsedLabel: String get() = formatStorageBytes(animWatchOfflineBytes)
    val cacheLabel: String get() = formatStorageBytes(appCacheBytes)
}

@Immutable
data class AppPermissionStatus(
    val id: String,
    val title: String,
    val description: String,
    val androidPermission: String?,
    val isGranted: Boolean,
    val canRequestRuntime: Boolean
)

data class OfflineSavedResult(
    val bytesWritten: Long,
    val localPath: String
)

fun formatStorageBytes(bytes: Long): String {
    val safe = bytes.coerceAtLeast(0L)
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
        safe >= gb -> String.format(Locale.US, "%.1f GB", safe / gb)
        safe >= mb -> String.format(Locale.US, "%.1f MB", safe / mb)
        safe >= kb -> String.format(Locale.US, "%.0f KB", safe / kb)
        else -> "$safe B"
    }
}

interface OfflineStorageGateway {
    fun inspectStorage(): DeviceStorageInfo
    fun readPermissions(): List<AppPermissionStatus>
    fun loadPersistedDownloads(): List<DownloadTask>
    suspend fun downloadEpisodePackage(
        anime: Anime,
        episode: Episode,
        qualityLabel: String,
        subtitleCues: List<SubtitleCue>,
        onProgress: (Float, Long) -> Unit
    ): Result<OfflineSavedResult>
    fun deleteEpisodePackage(episodeId: String): Boolean
    fun clearAppCache(): Long
}

class NoOpOfflineStorageGateway : OfflineStorageGateway {
    override fun inspectStorage(): DeviceStorageInfo = DeviceStorageInfo()
    override fun readPermissions(): List<AppPermissionStatus> = emptyList()
    override fun loadPersistedDownloads(): List<DownloadTask> = emptyList()
    override suspend fun downloadEpisodePackage(
        anime: Anime,
        episode: Episode,
        qualityLabel: String,
        subtitleCues: List<SubtitleCue>,
        onProgress: (Float, Long) -> Unit
    ): Result<OfflineSavedResult> = Result.success(OfflineSavedResult(0L, ""))
    override fun deleteEpisodePackage(episodeId: String): Boolean = true
    override fun clearAppCache(): Long = 0L
}

private data class PersistedDownloadRecord(
    val episodeId: String,
    val animeId: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val artThemeName: String,
    val qualityLabel: String,
    val posterUrl: String,
    val sizeBytes: Long,
    val localPath: String,
    val savedAtMs: Long
)

/**
 * Real Android device storage inspector, persistent offline episode package manager,
 * Coil/app cache cleaner, and runtime permission status reader.
 */
class AndroidDeviceStorageManager(context: Context) : OfflineStorageGateway {

    private val appContext: Context = context.applicationContext
    private val indexLock = Any()
    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val offlineRootDir: File
        get() {
            val base = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: appContext.filesDir
            return File(base, "AnimWatchOffline").apply { if (!exists()) mkdirs() }
        }

    private val indexFile: File
        get() = File(offlineRootDir, "downloads_index.json")

    private fun safeFolderName(id: String): String =
        id.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(80).ifEmpty { "ep_item" }

    private fun directorySizeBytes(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.walkTopDown().forEach { f ->
            if (f.isFile) total += f.length()
        }
        return total
    }

    override fun inspectStorage(): DeviceStorageInfo {
        val root = offlineRootDir
        val (totalBytes, availableBytes) = runCatching {
            val stat = StatFs(root.absolutePath)
            stat.totalBytes to stat.availableBytes
        }.getOrDefault(0L to 0L)

        val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
        val offlineBytes = directorySizeBytes(root)
        val cacheBytes = directorySizeBytes(appContext.cacheDir) +
            directorySizeBytes(appContext.externalCacheDir)

        val pathLabel = if (appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) != null) {
            "Penyimpanan Internal • Downloads/AnimWatch"
        } else {
            "Penyimpanan Internal Aplikasi"
        }

        return DeviceStorageInfo(
            totalDeviceBytes = totalBytes,
            availableDeviceBytes = availableBytes,
            usedDeviceBytes = usedBytes,
            animWatchOfflineBytes = offlineBytes,
            appCacheBytes = cacheBytes,
            storagePathLabel = pathLabel
        )
    }

    override fun readPermissions(): List<AppPermissionStatus> {
        val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else null
        val notifGranted = notifPermission == null ||
            ContextCompat.checkSelfPermission(appContext, notifPermission) == PackageManager.PERMISSION_GRANTED

        val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val mediaGranted = ContextCompat.checkSelfPermission(
            appContext,
            mediaPermission
        ) == PackageManager.PERMISSION_GRANTED

        val internetGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        val wakeLockGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.WAKE_LOCK
        ) == PackageManager.PERMISSION_GRANTED

        return listOf(
            AppPermissionStatus(
                id = "notifications",
                title = "Notifikasi Episode Terbaru",
                description = "Mengingatkan jadwal tayang & update episode anime terbaru",
                androidPermission = notifPermission,
                isGranted = notifGranted,
                canRequestRuntime = notifPermission != null && !notifGranted
            ),
            AppPermissionStatus(
                id = "storage",
                title = "Penyimpanan & Media Video",
                description = "Menyimpan paket episode offline, poster, dan membaca ruang perangkat",
                androidPermission = mediaPermission,
                isGranted = mediaGranted,
                canRequestRuntime = !mediaGranted
            ),
            AppPermissionStatus(
                id = "network",
                title = "Akses Internet & Status Jaringan",
                description = "Streaming episode dan sinkronisasi katalog anime secara online",
                androidPermission = Manifest.permission.INTERNET,
                isGranted = internetGranted,
                canRequestRuntime = false
            ),
            AppPermissionStatus(
                id = "wakelock",
                title = "Layar Tetap Menyala",
                description = "Mencegah layar mati otomatis selama pemutaran anime berlangsung",
                androidPermission = Manifest.permission.WAKE_LOCK,
                isGranted = wakeLockGranted,
                canRequestRuntime = false
            )
        )
    }

    private fun readIndexRecords(): MutableList<PersistedDownloadRecord> = synchronized(indexLock) {
        if (!indexFile.exists()) return mutableListOf()
        return runCatching {
            val type = object : TypeToken<List<PersistedDownloadRecord>>() {}.type
            gson.fromJson<List<PersistedDownloadRecord>>(indexFile.readText(), type).orEmpty().toMutableList()
        }.getOrDefault(mutableListOf())
    }

    private fun writeIndexRecords(records: List<PersistedDownloadRecord>) = synchronized(indexLock) {
        runCatching {
            indexFile.writeText(gson.toJson(records))
        }
    }

    override fun loadPersistedDownloads(): List<DownloadTask> = synchronized(indexLock) {
        val records = readIndexRecords()
        val valid = mutableListOf<PersistedDownloadRecord>()
        val tasks = mutableListOf<DownloadTask>()

        for (rec in records) {
            val dir = File(rec.localPath)
            if (dir.exists()) {
                val realBytes = directorySizeBytes(dir).coerceAtLeast(rec.sizeBytes)
                valid += rec.copy(sizeBytes = realBytes)
                val theme = AnimeArtTheme.entries.find { it.name == rec.artThemeName }
                    ?: AnimeArtTheme.CELESTIAL_ASTRAL
                tasks += DownloadTask(
                    episodeId = rec.episodeId,
                    animeId = rec.animeId,
                    animeTitle = rec.animeTitle,
                    episodeNumber = rec.episodeNumber,
                    episodeTitle = rec.episodeTitle,
                    artTheme = theme,
                    qualityLabel = rec.qualityLabel,
                    totalMb = (realBytes / (1024L * 1024L)).toInt().let { mb ->
                        if (mb == 0 && realBytes >= 512L * 1024L) 1 else mb
                    },
                    progress = 1.0f,
                    status = DownloadStatus.COMPLETED,
                    sizeBytes = realBytes,
                    posterUrl = rec.posterUrl,
                    localPath = rec.localPath
                )
            }
        }
        if (valid.size != records.size) {
            writeIndexRecords(valid)
        }
        tasks.sortedByDescending { t -> valid.find { it.episodeId == t.episodeId }?.savedAtMs ?: 0L }
    }

    override suspend fun downloadEpisodePackage(
        anime: Anime,
        episode: Episode,
        qualityLabel: String,
        subtitleCues: List<SubtitleCue>,
        onProgress: (Float, Long) -> Unit
    ): Result<OfflineSavedResult> = withContext(Dispatchers.IO) {
        try {
            val epDir = File(offlineRootDir, safeFolderName(episode.id)).apply { mkdirs() }
            var written = 0L
            var lastProgressEmitAt = 0L
            var lastProgressFrac = -1f

            fun emitProgress(frac: Float, bytes: Long, force: Boolean = false) {
                val now = System.currentTimeMillis()
                if (force || (frac - lastProgressFrac >= 0.02f && now - lastProgressEmitAt >= 80L)) {
                    lastProgressFrac = frac
                    lastProgressEmitAt = now
                    onProgress(frac, bytes)
                }
            }

            coroutineContext.ensureActive()
            emitProgress(0.15f, written, force = true)

            // 1. Save full structured offline package JSON (metadata, synopsis, official link, subtitles)
            val packagePayload = mapOf(
                "animeId" to anime.id,
                "animeTitle" to anime.title,
                "japaneseTitle" to anime.japaneseTitle,
                "studio" to anime.studio,
                "genres" to anime.genres,
                "episodeId" to episode.id,
                "episodeNumber" to episode.number,
                "episodeTitle" to episode.title,
                "episodeSynopsis" to episode.synopsis,
                "qualityPreference" to qualityLabel,
                "officialChannel" to (anime.officialChannel ?: "AnimWatch"),
                "streamType" to episode.streamType,
                "streamUrl" to episode.streamUrl,
                "subtitles" to subtitleCues.map {
                    mapOf(
                        "startSec" to it.startSec,
                        "endSec" to it.endSec,
                        "id" to it.indoText,
                        "en" to it.englishText
                    )
                },
                "savedAtMs" to System.currentTimeMillis()
            )
            val metaFile = File(epDir, "episode_package.json")
            metaFile.writeText(gson.toJson(packagePayload))
            written += metaFile.length()
            emitProgress(0.35f, written, force = true)

            // 2. Download real HD artwork/thumbnail asset to device storage with cooperative cancellation
            val artUrls = listOfNotNull(
                anime.posterUrl.takeIf { it.startsWith("http", ignoreCase = true) },
                anime.bannerUrl.takeIf { it.startsWith("http", ignoreCase = true) && it != anime.posterUrl }
            )
            val parentJob = coroutineContext[Job]
            artUrls.forEachIndexed { idx, imgUrl ->
                coroutineContext.ensureActive()
                val req = Request.Builder().url(imgUrl).build()
                val call = httpClient.newCall(req)
                val handle = parentJob?.invokeOnCompletion { cause ->
                    if (cause != null) call.cancel()
                }
                try {
                    call.execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body ?: return@use
                            val totalExpected = body.contentLength().coerceAtLeast(1L)
                            val outFile = File(epDir, if (idx == 0) "poster.jpg" else "banner.jpg")
                            body.byteStream().use { input ->
                                outFile.outputStream().use { output ->
                                    val buf = ByteArray(8192)
                                    var read: Int
                                    var fileBytes = 0L
                                    while (input.read(buf).also { read = it } != -1) {
                                        coroutineContext.ensureActive()
                                        output.write(buf, 0, read)
                                        fileBytes += read
                                        written += read
                                        val frac = (0.35f + 0.55f * ((idx + (fileBytes.toFloat() / totalExpected).coerceIn(0f, 1f)) / artUrls.size.coerceAtLeast(1)))
                                            .coerceIn(0.35f, 0.92f)
                                        emitProgress(frac, written)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    coroutineContext.ensureActive()
                } finally {
                    handle?.dispose()
                }
            }

            coroutineContext.ensureActive()
            val totalWritten = directorySizeBytes(epDir).coerceAtLeast(written)

            // 3. Persist index record atomically so downloads survive app restarts
            synchronized(indexLock) {
                coroutineContext.ensureActive()
                val existing = readIndexRecords().filterNot { it.episodeId == episode.id }.toMutableList()
                existing.add(
                    0,
                    PersistedDownloadRecord(
                        episodeId = episode.id,
                        animeId = anime.id,
                        animeTitle = anime.title,
                        episodeNumber = episode.number,
                        episodeTitle = episode.title,
                        artThemeName = anime.artTheme.name,
                        qualityLabel = qualityLabel,
                        posterUrl = anime.posterUrl,
                        sizeBytes = totalWritten,
                        localPath = epDir.absolutePath,
                        savedAtMs = System.currentTimeMillis()
                    )
                )
                writeIndexRecords(existing)
            }
            emitProgress(1.0f, totalWritten, force = true)

            Result.success(OfflineSavedResult(bytesWritten = totalWritten, localPath = epDir.absolutePath))
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override fun deleteEpisodePackage(episodeId: String): Boolean = synchronized(indexLock) {
        val records = readIndexRecords()
        val matched = records.find { it.episodeId == episodeId }
        val targetDir = if (matched != null) File(matched.localPath) else File(offlineRootDir, safeFolderName(episodeId))
        val deleted = if (targetDir.exists()) targetDir.deleteRecursively() else true
        writeIndexRecords(records.filterNot { it.episodeId == episodeId })
        deleted
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    override fun clearAppCache(): Long {
        val before = directorySizeBytes(appContext.cacheDir) + directorySizeBytes(appContext.externalCacheDir)
        runCatching {
            val loader = Coil.imageLoader(appContext)
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
        }
        val protectedNames = setOf("coil_poster_cache", "WebView", "org.chromium.android_webview")
        runCatching {
            appContext.cacheDir?.listFiles()?.forEach { child ->
                if (child.name !in protectedNames) {
                    child.deleteRecursively()
                }
            }
            appContext.externalCacheDir?.listFiles()?.forEach { child ->
                if (child.name !in protectedNames) {
                    child.deleteRecursively()
                }
            }
        }
        val after = directorySizeBytes(appContext.cacheDir) + directorySizeBytes(appContext.externalCacheDir)
        return (before - after).coerceAtLeast(0L)
    }

    companion object {
        fun openAppPermissionSettings(context: Context) {
            runCatching {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}
