package com.locked.app.data

/**
 * Hard-coded protected package names for v1. Deliberately not exposed in the
 * UI yet -- the architecture (a plain Set<String>) makes it trivial to grow
 * into a user-editable list later without touching the detection logic.
 */
object ProtectedApps {

    const val INSTAGRAM = "com.instagram.android"

    // TikTok has shipped under a couple of package names depending on region
    // / install source over the years. Both are treated as "TikTok".
    const val TIKTOK = "com.zhiliaoapp.musically"
    const val TIKTOK_ALT = "com.ss.android.ugc.trill"

    const val BRAVE = "com.brave.browser"

    val ALL: Set<String> = setOf(INSTAGRAM, TIKTOK, TIKTOK_ALT, BRAVE)

    fun isProtected(packageName: String?): Boolean =
        packageName != null && packageName in ALL

    fun displayName(packageName: String): String = when (packageName) {
        INSTAGRAM -> "Instagram"
        TIKTOK, TIKTOK_ALT -> "TikTok"
        BRAVE -> "Brave"
        else -> "App"
    }
}
