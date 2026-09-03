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
import kotlinx.coroutines.cancel
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
    @Volatile
    private var blockActivityPackage: String? = null
    @Volatile
    private var blockLaunchRequestedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        settingsRepository = SettingsRepository(applicationContext)
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) {
            // The block activity reports its own lifecycle separately. Its
            // accessibility events must not change the protected-package
            // session underneath it.
            return
        }

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
        if (blockActivityPackage == packageName) return
        if (blockLaunchRequestedPackage == packageName) return

        blockLaunchRequestedPackage = packageName
        serviceScope.launch {
            val protectionOn = settingsRepository.protectionEnabled.first()
            if (!protectionOn) {
                blockLaunchRequestedPackage = null
                return@launch
            }
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

    private fun onBlockActivityStarted(packageName: String) {
        blockActivityPackage = packageName
        blockLaunchRequestedPackage = null
    }

    private fun onBlockActivityStopped(packageName: String) {
        if (blockActivityPackage == packageName) {
            blockActivityPackage = null
            blockLaunchRequestedPackage = null
        } else if (blockLaunchRequestedPackage == packageName) {
            blockLaunchRequestedPackage = null
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ProtectionA11yService"

        @Volatile
        private var instance: ProtectionAccessibilityService? = null

        fun notifyBlockActivityStarted(packageName: String) {
            instance?.onBlockActivityStarted(packageName)
        }

        fun notifyBlockActivityStopped(packageName: String) {
            instance?.onBlockActivityStopped(packageName)
        }
    }
}
