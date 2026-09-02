package com.locked.app.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.locked.app.LockedApplication
import com.locked.app.R
import com.locked.app.data.NotificationMessages
import java.util.concurrent.TimeUnit

/**
 * Posts a single motivational notification, then WorkManager reschedules
 * this worker for another ~4 hours out. That cadence -- a handful of times
 * a day at most -- matches "occasional rather than spammy" from the spec.
 */
class MotivationNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return Result.success()

        val message = NotificationMessages.MESSAGES.random()

        val notification = NotificationCompat.Builder(context, LockedApplication.CHANNEL_MOTIVATION)
            .setContentTitle("Locked")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val WORK_NAME = "motivation_notification_work"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MotivationNotificationWorker>(
                4, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
