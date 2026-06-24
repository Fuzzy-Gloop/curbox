package neth.iecal.curbox.data.sync

import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * The durable backstop for sync. Realtime is not used on Android, so this
 * periodic pull and push keeps every device current even after the app was
 * killed.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        SyncGateway.init(applicationContext)
        val provider = SyncGateway.provider
        if (!provider.isAvailable) return Result.success()
        return try {
            provider.refresh()
            provider.pushNow()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val NAME = "curbox_sync"

        fun schedule(context: Context) {
            // This app does not ship the androidx.startup initializer, so make
            // sure WorkManager is up before we touch it. Call this off the main
            // thread, never from Application.onCreate.
            if (!WorkManager.isInitialized()) {
                WorkManager.initialize(context.applicationContext, Configuration.Builder().build())
            }
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
