package com.locked.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.locked.app.data.ProtectedApps

@Composable
fun SettingsScreen(
    protectionEnabled: Boolean,
    onProtectionEnabledChange: (Boolean) -> Unit,
    morningEnabled: Boolean,
    onMorningEnabledChange: (Boolean) -> Unit,
    userName: String,
    onUserNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        SectionLabel("Protection")
        SettingsSwitchRow(
            title = "Protection",
            checked = protectionEnabled,
            onCheckedChange = onProtectionEnabledChange
        )

        Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
            ProtectedRow(ProtectedApps.displayName(ProtectedApps.INSTAGRAM))
            ProtectedRow(ProtectedApps.displayName(ProtectedApps.TIKTOK))
            ProtectedRow(ProtectedApps.displayName(ProtectedApps.BRAVE))
            ProtectedRow(ProtectedApps.displayName(ProtectedApps.YOUTUBE))
            ProtectedRow(ProtectedApps.displayName(ProtectedApps.X))
        }

        HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFF262626), modifier = Modifier.padding(vertical = 24.dp))

        SectionLabel("Morning Session")
        SettingsSwitchRow(
            title = "Morning self-hypnosis",
            checked = morningEnabled,
            onCheckedChange = onMorningEnabledChange
        )

        Text(
            text = "Your name",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Used in the morning script") },
            singleLine = true
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

@Composable
private fun ProtectedRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83D\uDD12  ", color = MaterialTheme.colorScheme.outline)
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
