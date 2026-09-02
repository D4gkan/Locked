package com.locked.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.locked.app.data.ProtectedApps

@Composable
fun HomeScreen(
    protectionEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "LOCKED",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Stay focused.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            val dotColor = if (protectionEnabled) Color(0xFFB33A3A) else MaterialTheme.colorScheme.outline
            Text(text = "●", color = dotColor, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (protectionEnabled) " PROTECTION ACTIVE" else " PROTECTION PAUSED",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        HorizontalDivider(color = Color(0xFF262626))
        Spacer(modifier = Modifier.height(24.dp))

        ProtectedAppRow(ProtectedApps.displayName(ProtectedApps.INSTAGRAM))
        ProtectedAppRow(ProtectedApps.displayName(ProtectedApps.TIKTOK))
        ProtectedAppRow(ProtectedApps.displayName(ProtectedApps.BRAVE))

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onOpenSettings) {
            Text(
                text = "Settings",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProtectedAppRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83D\uDD12  ", color = MaterialTheme.colorScheme.outline)
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
