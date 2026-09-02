package com.locked.app.ui.block

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.locked.app.data.MotivationalMessages
import com.locked.app.ui.theme.LockedTheme
import com.locked.app.unlock.UnlockState

/**
 * Full-screen activity shown the instant a protected package reaches the
 * foreground. Owns the whole sequence: motivational messages -> LOCKED
 * screen with 20s hold -> confirmation -> finish() (which reveals the
 * protected app underneath, since it was never destroyed -- only this
 * activity's task was drawn on top of it).
 *
 * launchMode singleTask + FLAG_ACTIVITY_CLEAR_TOP/SINGLE_TOP in the launch
 * intent (see ProtectionAccessibilityService) mean rapid repeated launches
 * collapse onto the same instance instead of stacking duplicate activities.
 */
class BlockActivity : ComponentActivity() {

    private var blockedPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""

        // Show above the lock screen if needed, and make sure the screen is
        // actually on -- belt-and-suspenders alongside the manifest flags,
        // since some OEM skins are inconsistent about honoring those alone.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        enableEdgeToEdge()

        // Back is intentionally NOT a bypass: it just backgrounds the task
        // rather than revealing the protected app, and never marks anything
        // as unlocked. Handled via the dispatcher (not a deprecated
        // onBackPressed override) so it also works with predictive back.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        setContent {
            LockedTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black)
                ) {
                    BlockFlow(
                        onUnlockGranted = {
                            UnlockState.markUnlocked(blockedPackage)
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // A repeated launch (e.g. the protected app was brought to the
        // foreground again while we're already showing) should restart the
        // whole sequence from the top, not resume mid-way.
        blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: blockedPackage
        recreate()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
    }
}

private enum class BlockStage { MESSAGES, LOCK, CONFIRM }

@androidx.compose.runtime.Composable
private fun BlockFlow(onUnlockGranted: () -> Unit) {
    var stage by remember { mutableStateOf(BlockStage.MESSAGES) }

    when (stage) {
        BlockStage.MESSAGES -> com.locked.app.util.FadeMessageSequence(
            lines = MotivationalMessages.SEQUENCE,
            holdMillis = MotivationalMessages.VISIBLE_MS,
            fadeInMillis = MotivationalMessages.FADE_IN_MS.toInt(),
            fadeOutMillis = MotivationalMessages.FADE_OUT_MS.toInt(),
            onFinished = { stage = BlockStage.LOCK }
        )

        BlockStage.LOCK -> LockScreen(
            onHoldComplete = { stage = BlockStage.CONFIRM }
        )

        BlockStage.CONFIRM -> ConfirmationScreen(
            onConfirm = onUnlockGranted
        )
    }
}
