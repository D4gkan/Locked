package com.locked.app.unlock

/**
 * Tracks which protected package (if any) is currently allowed through,
 * for the current foreground session only. Intentionally in-memory only --
 * nothing here is written to disk, so there is no way for an "unlocked"
 * state to survive a re-launch, a process death, or a reboot. That is by
 * design: every fresh opening of a protected app must repeat the full
 * sequence.
 */
object UnlockState {

    @Volatile
    private var unlockedPackage: String? = null

    fun isUnlocked(packageName: String): Boolean = unlockedPackage == packageName

    fun markUnlocked(packageName: String) {
        unlockedPackage = packageName
    }

    /** Call whenever the foreground leaves a protected package. */
    fun clear() {
        unlockedPackage = null
    }
}
