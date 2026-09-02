package com.locked.app.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locked.app.data.SettingsRepository
import com.locked.app.service.ProtectionForegroundService
import com.locked.app.ui.settings.SettingsActivity
import com.locked.app.ui.theme.LockedTheme
import com.locked.app.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshPermissionState() }

    private var refreshTrigger = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        enableEdgeToEdge()

        setContent {
            LockedTheme {
                val trigger by refreshTrigger
                val protectionEnabled by settingsRepository.protectionEnabled
                    .collectAsStateWithLifecycle(initialValue = true)

                val accessibilityGranted = remember(trigger) {
                    PermissionHelper.isAccessibilityServiceEnabled(this)
                }
                val overlayGranted = remember(trigger) {
                    PermissionHelper.canDrawOverlays(this)
                }
                val notificationGranted = remember(trigger) {
                    PermissionHelper.hasNotificationPermission(this)
                }

                if (accessibilityGranted && overlayGranted && notificationGranted) {
                    if (protectionEnabled) {
                        ProtectionForegroundService.start(this)
                    }
                    HomeScreen(
                        protectionEnabled = protectionEnabled,
                        onOpenSettings = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }
                    )
                } else {
                    OnboardingScreen(
                        accessibilityGranted = accessibilityGranted,
                        overlayGranted = overlayGranted,
                        notificationGranted = notificationGranted,
                        onRequestAccessibility = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onRequestOverlay = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                            )
                        },
                        onRequestNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                refreshPermissionState()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The user typically grants accessibility/overlay permissions in
        // Settings and returns here via Back -- re-check on every resume.
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        refreshTrigger.value += 1
    }
}
