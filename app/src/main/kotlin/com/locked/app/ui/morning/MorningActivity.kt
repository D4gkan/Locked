package com.locked.app.ui.morning

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.locked.app.data.MorningScript
import com.locked.app.data.NarrationLine
import com.locked.app.data.SettingsRepository
import com.locked.app.ui.theme.LockedTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * Full-screen calming narration shown once per configured morning window.
 * Not designed to be inescapable like BlockActivity -- a delayed Skip
 * affordance is intentionally present (see MorningScript.SKIP_AVAILABLE_AFTER_MS)
 * so a genuine emergency (a call to make, an alarm running late) is never
 * blocked by a wellness feature.
 */
class MorningActivity : ComponentActivity() {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var musicPlayer: MediaPlayer? = null
    private lateinit var settingsRepository: SettingsRepository

    // Bridges TTS's utterance-completion callback (fired on a non-Compose
    // thread) into the suspend-based narration loop below.
    private val utteranceDone = Channel<Unit>(Channel.CONFLATED)

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
                // Back behaves like Skip -- this is a wellness screen, not
                // a lock, so there is no reason to trap the user here.
                finishSession()
            }
        })

        initTts()
        startAmbientMusic()

        setContent {
            LockedTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    MorningScreen(
                        settingsRepository = settingsRepository,
                        onSpeak = { text, utteranceId -> speak(text, utteranceId) },
                        awaitUtteranceDone = { utteranceDone.receive() },
                        onFinished = { finishSession() },
                        onSkip = { finishSession() }
                    )
                }
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setSpeechRate(0.85f)
                tts?.setPitch(0.95f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        utteranceDone.trySend(Unit)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        utteranceDone.trySend(Unit)
                    }
                })
                ttsReady = true
            }
        }
    }

    private fun speak(text: String, utteranceId: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            // TTS engine not ready (rare) -- fall back to a fixed delay so
            // the visual sequence still proceeds at a reasonable pace.
            CoroutineScope(Dispatchers.Default).launch {
                delay(1800)
                utteranceDone.trySend(Unit)
            }
        }
    }

    /**
     * Loops a bundled ambient bed under the narration, fading in over 2s.
     * No royalty-free track is bundled by default -- drop one at
     * app/src/main/assets/morning_ambient.mp3 and this picks it up
     * automatically. Missing asset -> logged and silently skipped, never a
     * crash.
     */
    private fun startAmbientMusic() {
        try {
            val afd = assets.openFd("morning_ambient.mp3")
            musicPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                setVolume(0f, 0f)
                prepare()
                start()
            }
            fadeMusicVolume(target = 0.35f, durationMs = 2000)
        } catch (e: Exception) {
            // No asset present yet -- narration still works fine without
            // background music.
        }
    }

    private fun fadeMusicVolume(target: Float, durationMs: Long) {
        val player = musicPlayer ?: return
        lifecycleScope.launch {
            val steps = 20
            val stepDelay = durationMs / steps
            val current = 0f
            for (i in 1..steps) {
                val v = current + (target - current) * (i / steps.toFloat())
                player.setVolume(v, v)
                delay(stepDelay)
            }
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
        tts?.stop()
        tts?.shutdown()
        stopAmbientMusic()
        super.onDestroy()
    }
}

@Composable
private fun MorningScreen(
    settingsRepository: SettingsRepository,
    onSpeak: (String, String) -> Unit,
    awaitUtteranceDone: suspend () -> Unit,
    onFinished: () -> Unit,
    onSkip: () -> Unit
) {
    var script by remember { mutableStateOf<List<NarrationLine>>(emptyList()) }
    var lineIndex by remember { mutableIntStateOf(-1) }
    var alpha by remember { mutableFloatStateOf(0f) }
    var skipVisible by remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 700),
        label = "narrationFade"
    )

    LaunchedEffect(Unit) {
        val name = settingsRepository.userName.first()
        script = MorningScript.build(name)
    }

    LaunchedEffect(Unit) {
        delay(MorningScript.SKIP_AVAILABLE_AFTER_MS)
        skipVisible = true
    }

    LaunchedEffect(script) {
        if (script.isEmpty()) return@LaunchedEffect
        for (i in script.indices) {
            lineIndex = i
            alpha = 1f
            val utteranceId = UUID.randomUUID().toString()
            onSpeak(script[i].text, utteranceId)
            awaitUtteranceDone()
            delay((script[i].pauseAfterSeconds * 1000).toLong())
            alpha = 0f
            delay(500)
        }
        onFinished()
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

        if (skipVisible) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
