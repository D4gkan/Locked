package com.locked.app.data

/** One spoken/displayed line, with a pause (in seconds) held after it. */
data class NarrationLine(
    val text: String,
    val pauseAfterSeconds: Float = 0.4f
)

/**
 * Builds the morning script with the user's name interpolated. Edit the
 * text freely -- the player just walks this list top to bottom, speaking
 * each line via TextToSpeech and holding for pauseAfterSeconds afterward.
 */
object MorningScript {

    fun build(name: String): List<NarrationLine> {
        val greeting = if (name.isNotBlank()) "Good morning, $name." else "Good morning."

        return listOf(
            NarrationLine(greeting, 1.5f),
            NarrationLine("Before you begin your day, take a moment for yourself.", 1f),
            NarrationLine("There is nowhere you need to be right now.", 1f),
            NarrationLine("Nothing you need to solve. Nothing you need to worry about.", 1f),
            NarrationLine("Just breathe, and listen to my voice.", 1f),
            NarrationLine("Close your eyes, and take a slow, deep breath in.", 3f),
            NarrationLine("And slowly breathe out.", 3f),
            NarrationLine("Again, breathe in deeply. Feel the air filling your lungs.", 1f),
            NarrationLine("And breathe out, letting your body become a little more relaxed.", 4f),
            NarrationLine("With every breath, you become calmer, more focused, and more present.", 1f),
            NarrationLine("Relax your shoulders. Relax your face. Let your jaw become loose. Let your hands rest comfortably.", 1f),
            NarrationLine("You don't need to do anything. Just listen.", 5f),
            NarrationLine("Now imagine yourself moving through today.", 1f),
            NarrationLine("See yourself getting up, feeling refreshed, feeling clear, feeling ready.", 1f),
            NarrationLine("And as you move through your morning, you begin to notice something.", 1f),
            NarrationLine("You have energy. You have focus. You have confidence.", 3f),
            NarrationLine("Today is going to be a great day. A genuinely great day.", 1f),
            NarrationLine("You are ready for it. You are capable. You are focused. You are calm.", 1f),
            NarrationLine("And you are in control of where you put your attention.", 3f),
            NarrationLine("Today, you will be productive. You will make progress.", 1f),
            NarrationLine("You will focus on what matters, and let go of what doesn't.", 1f),
            NarrationLine("You don't need to do everything at once.", 1f),
            NarrationLine("One thing at a time. One decision at a time. One step at a time.", 1f),
            NarrationLine("And every small step moves you forward.", 3f),
            NarrationLine("Today, you will be creative. Ideas will come naturally to you.", 1f),
            NarrationLine("You will notice possibilities you might normally overlook.", 1f),
            NarrationLine("You will think differently. You will find solutions.", 1f),
            NarrationLine("You will allow yourself to experiment, to create, and to explore.", 3f),
            NarrationLine("Today, you will have energy. Your body feels awake. Your mind feels alert.", 1f),
            NarrationLine("You feel motivated to move, to work, to create, and to experience the day.", 1f),
            NarrationLine("You don't need to force yourself. You simply begin.", 1f),
            NarrationLine("And once you begin, momentum follows.", 3f),
            NarrationLine("Today, you will be confident. You trust yourself. You trust your decisions.", 1f),
            NarrationLine("You don't need everyone's approval. You don't need everything to be perfect.", 1f),
            NarrationLine("You simply need to keep moving forward.", 3f),
            NarrationLine("If something difficult happens today, you will breathe. You will stay calm.", 1f),
            NarrationLine("You will think clearly, and you will handle it one step at a time.", 1f),
            NarrationLine("Nothing needs to ruin your entire day.", 1f),
            NarrationLine("You can pause, reset, and continue.", 4f),
            NarrationLine("You are leaving yesterday behind. Yesterday is finished.", 1f),
            NarrationLine("This is a new day. A fresh start. A new opportunity.", 1f),
            NarrationLine("And you are ready to use it.", 4f),
            NarrationLine("So remember: you are focused, productive, creative, and energetic.", 1f),
            NarrationLine("You are confident, capable, resilient, and ready.", 4f),
            NarrationLine("Today will be a great day. You will accomplish things that matter.", 1f),
            NarrationLine("You will create something. You will learn something. You will make progress.", 1f),
            NarrationLine("And at the end of the day, you will be proud that you started.", 3f),
            NarrationLine("Now, take one final deep breath in.", 3f),
            NarrationLine("And slowly breathe out.", 3f),
            NarrationLine("When you open your eyes, bring this feeling with you.", 1f),
            NarrationLine("The calm. The focus. The energy. The confidence.", 1f),
            NarrationLine("You don't have to wait for motivation. You create momentum by taking the first step.", 1f),
            NarrationLine("And you're ready to take it.", 3f),
            NarrationLine("In three, two, one, open your eyes.", 2f),
            NarrationLine("$greeting Today is yours. Go make it a great one.", 1f)
        )
    }

    /** Skip affordance only appears after this many milliseconds. */
    const val SKIP_AVAILABLE_AFTER_MS = 10_000L
}
