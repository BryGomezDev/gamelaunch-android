package com.gamelaunch

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.gamelaunch.BuildConfig
import com.gamelaunch.notification.NotificationChannels
import com.gamelaunch.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject

@HiltAndroidApp
class GameLaunchApp : Application(), Configuration.Provider {

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
}
