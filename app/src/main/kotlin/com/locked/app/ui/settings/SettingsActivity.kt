package com.locked.app.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.locked.app.data.SettingsRepository
import com.locked.app.service.ProtectionForegroundService
import com.locked.app.ui.theme.LockedTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)
        enableEdgeToEdge()

        setContent {
            LockedTheme {
                val protectionEnabled by settingsRepository.protectionEnabled
                    .collectAsStateWithLifecycle(initialValue = true)
                val morningEnabled by settingsRepository.morningEnabled
                    .collectAsStateWithLifecycle(initialValue = true)
                val userName by settingsRepository.userName
                    .collectAsStateWithLifecycle(initialValue = "")

                SettingsScreen(
                    protectionEnabled = protectionEnabled,
                    onProtectionEnabledChange = { enabled ->
                        lifecycleScope.launch {
                            settingsRepository.setProtectionEnabled(enabled)
                            if (enabled) {
                                ProtectionForegroundService.start(this@SettingsActivity)
                            } else {
                                ProtectionForegroundService.stop(this@SettingsActivity)
                            }
                        }
                    },
                    morningEnabled = morningEnabled,
                    onMorningEnabledChange = { enabled ->
                        lifecycleScope.launch {
                            settingsRepository.setMorningEnabled(enabled)
                        }
                    },
                    userName = userName,
                    onUserNameChange = { name ->
                        lifecycleScope.launch {
                            settingsRepository.setUserName(name)
                        }
                    }
                )
            }
        }
    }
}
