package com.locked.app.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.locked.app.LockedApplication
import com.locked.app.R
import com.locked.app.data.SettingsRepository
import com.locked.app.ui.morning.MorningActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * A lightweight always-on foreground service. Its jobs:
 *  1. Hold a persistent low-priority notification so OEM battery managers
 *     (Samsung's especially) are much less likely to kill the process the
 *     AccessibilityService relies on.
 *  2. Listen for ACTION_USER_PRESENT (fired right after the keyguard is
 *     dismissed -- more precise than ACTION_SCREEN_ON, which can fire while
 *     still locked) to decide whether to launch the morning session.
 *
 * It does NOT do the app-foreground detection itself -- that stays fully
 * inside ProtectionAccessibilityService, which is the event-driven,
 * battery-friendly mechanism for that job.
 */
class ProtectionForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var settingsRepository: SettingsRepository
    private var receiverRegistered = false

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                checkAndMaybeLaunchMorningSession()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        val notificationType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), notificationType)

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(userPresentReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(userPresentReceiver, filter)
        }
        receiverRegistered = true
        MotivationNotificationWorker.schedule(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: ask the system to recreate this service if it's
        // killed under memory pressure, with a null intent next time.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): android.os.IBinder? = null

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(userPresentReceiver)
        }
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, LockedApplication.CHANNEL_PROTECTION)
            .setContentTitle(getString(R.string.notif_protection_title))
            .setContentText(getString(R.string.notif_protection_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun checkAndMaybeLaunchMorningSession() {
        serviceScope.launch {
            val enabled = settingsRepository.morningEnabled.first()
            if (!enabled) return@launch

            val today = LocalDate.now()
            val lastDone = settingsRepository.lastMorningSessionDate.first()
            if (lastDone == today.toString()) return@launch

            val start = settingsRepository.morningWindowStart.first()
            val end = settingsRepository.morningWindowEnd.first()
            val now = LocalTime.now()
            val withinWindow = if (start <= end) {
                now >= start && now <= end
            } else {
                // Window wraps past midnight -- not expected with the
                // 4am-11am default, but handled for custom windows.
                now >= start || now <= end
            }
            if (!withinWindow) return@launch

            val intent = Intent(this@ProtectionForegroundService, MorningActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, ProtectionForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ProtectionForegroundService::class.java))
            MotivationNotificationWorker.cancel(context)
        }
    }
}
