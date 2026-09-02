package com.locked.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LockedColorScheme = darkColorScheme(
    primary = OffWhite,
    onPrimary = AlmostBlack,
    secondary = DisciplineRed,
    onSecondary = OffWhite,
    background = AlmostBlack,
    onBackground = OffWhite,
    surface = SurfaceDark,
    onSurface = OffWhite,
    outline = MutedGray
)

@Composable
fun LockedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LockedColorScheme,
        typography = LockedTypography,
        content = content
    )
}
