package com.locked.app.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Plays through [lines] one at a time: fade in, hold, fade out, next line.
 * Calls [onFinished] once after the last line's fade-out completes.
 *
 * Deliberately slow and calm per the design spec: no bounce, no easing
 * tricks, just a plain linear-ish fade.
 */
@Composable
fun FadeMessageSequence(
    lines: List<String>,
    holdMillis: Long,
    fadeInMillis: Int = 650,
    fadeOutMillis: Int = 650,
    onFinished: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var alpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = if (alpha > 0f) fadeInMillis else fadeOutMillis),
        label = "messageFade"
    )

    LaunchedEffect(lines) {
        for (i in lines.indices) {
            index = i
            alpha = 1f
            delay(fadeInMillis.toLong())
            delay(holdMillis)
            alpha = 0f
            delay(fadeOutMillis.toLong())
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (index in lines.indices) {
            Text(
                text = lines[index],
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { this.alpha = animatedAlpha }
            )
        }
    }
}
