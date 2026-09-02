package com.locked.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.locked.app.data.ProtectedApps
import com.locked.app.data.SettingsRepository
import com.locked.app.ui.block.BlockActivity
import com.locked.app.unlock.UnlockState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The detection core of the app. Uses TYPE_WINDOW_STATE_CHANGED events --
 * an event-driven callback, not polling -- to notice when the foreground
 * package changes. When it changes *into* a protected package, and that
 * package isn't already unlocked for this foreground session, it launches
 * BlockActivity immediately. When it changes *away* from a protected
 * package, it clears the one-time unlock so the next entry starts over.
 *
 * canRetrieveWindowContent is false in the service config: this service
 * never inspects on-screen content, only which package is in the
 * foreground, which is the minimum needed for this feature.
 */
class ProtectionAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var settingsRepository: SettingsRepository

    private var lastForegroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        settingsRepository = SettingsRepository(applicationContext)
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) {
            // Ignore our own UI (e.g. the block screen itself) becoming
            // foreground -- it is not something to react to.
            return
        }

        if (packageName == lastForegroundPackage) return
        val previousPackage = lastForegroundPackage
        lastForegroundPackage = packageName

        if (ProtectedApps.isProtected(previousPackage) && !ProtectedApps.isProtected(packageName)) {
            UnlockState.clear()
        }

        if (ProtectedApps.isProtected(packageName)) {
            handleProtectedPackageForeground(packageName)
        }
    }

    private fun handleProtectedPackageForeground(packageName: String) {
        if (UnlockState.isUnlocked(packageName)) return

        serviceScope.launch {
            val protectionOn = settingsRepository.protectionEnabled.first()
            if (!protectionOn) return@launch
            launchBlockScreen(packageName)
        }
    }

    private fun launchBlockScreen(packageName: String) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    companion object {
        private const val TAG = "ProtectionA11yService"
    }
}
