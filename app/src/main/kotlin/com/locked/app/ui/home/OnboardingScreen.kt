package com.locked.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Shown until every permission Locked actually needs has been granted.
 * Each step is a plain button to the relevant system screen -- no
 * decoration, no gamified checklist, matching the rest of the app's
 * design language.
 */
@Composable
fun OnboardingScreen(
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    notificationGranted: Boolean,
    onRequestAccessibility: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotifications: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LOCKED",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "A few permissions are needed before protection can start.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        PermissionRow(
            title = "Accessibility access",
            description = "Lets Locked notice when Instagram, TikTok, Brave, YouTube, or X open.",
            granted = accessibilityGranted,
            onRequest = onRequestAccessibility
        )
        PermissionRow(
            title = "Display over other apps",
            description = "Lets the block screen appear instantly, above the app you opened.",
            granted = overlayGranted,
            onRequest = onRequestOverlay
        )
        PermissionRow(
            title = "Notifications",
            description = "For the occasional motivational reminder.",
            granted = notificationGranted,
            onRequest = onRequestNotifications
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )
        if (granted) {
            Text(
                text = "Granted",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            OutlinedButton(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
                Text("Grant")
            }
        }
    }
}
