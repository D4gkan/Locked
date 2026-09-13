package com.locked.app.ui.morning

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.locked.app.data.MorningScript
import com.locked.app.data.NarrationLine
import com.locked.app.data.SettingsRepository
import com.locked.app.ui.theme.LockedTheme
import com.locked.app.util.RandomAudioAsset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Full-screen calming narration shown once per configured morning window.
 * Behaves like BlockActivity while the session is running: Back and task
 * navigation cannot dismiss the session before the narration completes.
 */
class MorningActivity : ComponentActivity() {

    private var musicPlayer: MediaPlayer? = null
    private var meditationDurationMs by mutableLongStateOf(0L)
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(applicationContext)

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })

        startMeditationMedia()

        setContent {
            LockedTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    MorningScreen(
                        settingsRepository = settingsRepository,
                        mediaDurationMs = meditationDurationMs,
                        onFinished = { finishSession() }
                    )
                }
            }
        }
    }

    private fun startMeditationMedia() {
        try {
            val assetName = RandomAudioAsset.chooseForDay(
                assetManager = assets,
                directory = "morning",
                extensions = setOf("mp3", "mp4"),
                dayOfWeek = LocalDate.now().dayOfWeek
            ) ?: return
            val afd = assets.openFd("morning/$assetName")
            musicPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                setOnCompletionListener { finishSession() }
                prepare()
                meditationDurationMs = duration.toLong()
                start()
            }
            afd.close()
        } catch (e: Exception) {
            meditationDurationMs = 0L
        }
    }

    private fun stopAmbientMusic() {
        musicPlayer?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                // Already released or never fully prepared -- fine to ignore.
            }
        }
        musicPlayer = null
    }

    private fun finishSession() {
        lifecycleScope.launch {
            settingsRepository.markMorningSessionDone()
        }
        stopAmbientMusic()
        finish()
    }

    override fun onDestroy() {
        stopAmbientMusic()
        super.onDestroy()
    }
}

@Composable
private fun MorningScreen(
    settingsRepository: SettingsRepository,
    mediaDurationMs: Long,
    onFinished: () -> Unit
) {
    var script by remember { mutableStateOf<List<NarrationLine>>(emptyList()) }
    var lineIndex by remember { mutableIntStateOf(-1) }
    var alpha by remember { mutableFloatStateOf(0f) }

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 700),
        label = "narrationFade"
    )

    LaunchedEffect(Unit) {
        val name = settingsRepository.userName.first()
        script = MorningScript.build(name)
    }

    LaunchedEffect(script, mediaDurationMs) {
        if (script.isEmpty()) return@LaunchedEffect
        val lineDurationMs = if (mediaDurationMs > 0L) {
            (mediaDurationMs / script.size).coerceAtLeast(1000L)
        } else {
            3000L
        }
        for (i in script.indices) {
            lineIndex = i
            alpha = 1f
            delay(lineDurationMs)
            alpha = 0f
            delay(500)
        }
        if (mediaDurationMs <= 0L) onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (lineIndex in script.indices) {
                Text(
                    text = script[lineIndex].text,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { this.alpha = animatedAlpha }
                )
            }
        }

    }
}
