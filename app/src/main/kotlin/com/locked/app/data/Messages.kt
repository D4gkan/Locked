package com.locked.app.data

/**
 * The message sequence shown one-by-one before the LOCKED screen. Edit this
 * list freely -- nothing else in the app needs to change.
 */
object MotivationalMessages {
    val SEQUENCE: List<String> = listOf(
        "Did you intend to enter this app?",
        "Are you sure you're locked in?",
        "Is it worth it?",
        "Do something productive."
    )

    /** How long each message stays fully visible, not counting fade time. */
    const val VISIBLE_MS = 2000L
    const val FADE_IN_MS = 650L
    const val FADE_OUT_MS = 650L
}

/**
 * Occasional motivational notifications. Edit this list freely.
 */
object NotificationMessages {
    val MESSAGES: List<String> = listOf(
        "Stay locked in.",
        "Discipline beats motivation.",
        "Your future self is watching what you do right now.",
        "Do the work.",
        "Stop scrolling. Start building.",
        "One more hour of focus can change your day.",
        "Comfort is expensive.",
        "Don't trade your goals for five minutes of dopamine.",
        "Get up. Do something productive."
    )
}
