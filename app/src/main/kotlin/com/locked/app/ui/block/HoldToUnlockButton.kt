package com.locked.app.ui.block

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

private const val HOLD_DURATION_MS = 30_000L
private const val TICK_MS = 16L

/**
 * A button that must be continuously pressed for exactly HOLD_DURATION_MS.
 * Releasing even a fraction of a second early resets progress to zero --
 * there is no partial credit, no pause, and no alternate input path.
 *
 * Also resets whenever the hosting lifecycle leaves RESUMED (screen turns
 * off, app loses focus, another window covers this one), per spec.
 */
@Composable
fun HoldToUnlockButton(
    modifier: Modifier = Modifier,
    messageLines: List<String> = emptyList(),
    onHoldComplete: () -> Unit
) {
    var elapsedMs by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }
    var messageIndex by remember { mutableIntStateOf(0) }
    var messageAlpha by remember { mutableFloatStateOf(1f) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isPressed = false
                elapsedMs = 0f
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isPressed) {
        if (!isPressed) {
            elapsedMs = 0f
            return@LaunchedEffect
        }
        while (isPressed && elapsedMs < HOLD_DURATION_MS) {
            delay(TICK_MS)
            if (isPressed) {
                elapsedMs += TICK_MS
            }
        }
        if (isPressed && elapsedMs >= HOLD_DURATION_MS) {
            onHoldComplete()
        }
    }

    LaunchedEffect(messageIndex, isPressed) {
        if (!isPressed || messageLines.isEmpty()) {
            messageAlpha = 1f
            return@LaunchedEffect
        }
        messageAlpha = 0f
        delay(100L)
        messageAlpha = 1f
    }

    LaunchedEffect(isPressed) {
        if (!isPressed || messageLines.isEmpty()) return@LaunchedEffect
        messageIndex = 0
        while (isPressed) {
            delay(2_600L)
            if (isPressed) {
                messageIndex = (messageIndex + 1) % messageLines.size
            }
        }
    }

    val progress = (elapsedMs / HOLD_DURATION_MS).coerceIn(0f, 1f)
    val secondsShown = (elapsedMs / 1000f).coerceAtMost(30f)
    val animatedMessageAlpha by animateFloatAsState(
        targetValue = messageAlpha,
        animationSpec = tween(durationMillis = 650),
        label = "lockMessageFade"
    )

    Box(
        modifier = modifier
            .size(220.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val released = tryAwaitRelease()
                        // Any release before completion -- deliberate or a
                        // dropped touch -- resets fully. No grace period.
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(220.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.onBackground,
            trackColor = Color(0xFF262626)
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isPressed && messageLines.isNotEmpty()) {
                        messageLines[messageIndex]
                    } else if (isPressed) {
                        "HOLDING"
                    } else {
                        "HOLD TO UNLOCK"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (isPressed && messageLines.isNotEmpty()) {
                            animatedMessageAlpha
                        } else {
                            1f
                        }
                    }
                )
                if (isPressed) {
                    Text(
                        text = "%.1f / 30.0".format(secondsShown),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
