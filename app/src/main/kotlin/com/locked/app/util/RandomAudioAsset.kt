package com.locked.app.util

import android.content.res.AssetManager
import java.time.DayOfWeek

object RandomAudioAsset {

    fun choose(
        assetManager: AssetManager,
        directory: String,
        extensions: Set<String>
    ): String? {
        return assetManager.list(directory)
            ?.filter {
                it.substringAfterLast('.', "").lowercase() in extensions
            }
            ?.takeIf { it.isNotEmpty() }
            ?.random()
    }

    fun chooseForDay(
        assetManager: AssetManager,
        directory: String,
        extensions: Set<String>,
        dayOfWeek: DayOfWeek
    ): String? {
        val matchingAssets = assetManager.list(directory)
            ?.filter {
                it.substringAfterLast('.', "").lowercase() in extensions
            }
            ?.sorted()
            ?: return null

        return matchingAssets.getOrNull(dayOfWeek.value - 1)
    }
}
