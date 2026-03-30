package com.gamelaunch

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.gamelaunch.BuildConfig
import com.gamelaunch.notification.NotificationChannels
import com.gamelaunch.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject

@HiltAndroidApp
class GameLaunchApp : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.tracesSampleRate = if (BuildConfig.DEBUG) 1.0 else 0.2
            options.isEnableUserInteractionTracing = true
            options.environment = if (BuildConfig.DEBUG) "debug" else "production"
        }
        NotificationChannels.create(this)
        SyncWorker.schedule(WorkManager.getInstance(this))
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.20) // 20% of available RAM
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                .build()
        }
        .crossfade(true)
        .respectCacheHeaders(false) // IGDB CDN headers vary; ignore and always cache
        .build()
}
