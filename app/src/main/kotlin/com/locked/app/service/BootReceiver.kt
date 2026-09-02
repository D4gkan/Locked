package com.locked.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.locked.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val repository = SettingsRepository(appContext)

        // A BroadcastReceiver's process can be killed the moment onReceive
        // returns, so we can't just fire-and-forget a coroutine here --
        // goAsync() keeps the receiver (and process) alive long enough for
        // the DataStore read to finish and the service to actually start.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val enabled = repository.protectionEnabled.first()
                if (enabled) {
                    ProtectionForegroundService.start(appContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
