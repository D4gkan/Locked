package com.locked.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class LockedApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)

        val protectionChannel = NotificationChannel(
            CHANNEL_PROTECTION,
            getString(R.string.notif_channel_protection_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notif_channel_protection_desc)
            setShowBadge(false)
        }

        val motivationChannel = NotificationChannel(
            CHANNEL_MOTIVATION,
            getString(R.string.notif_channel_motivation_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notif_channel_motivation_desc)
        }

        manager.createNotificationChannels(listOf(protectionChannel, motivationChannel))
    }

    companion object {
        const val CHANNEL_PROTECTION = "locked_protection_channel"
        const val CHANNEL_MOTIVATION = "locked_motivation_channel"
    }
}
