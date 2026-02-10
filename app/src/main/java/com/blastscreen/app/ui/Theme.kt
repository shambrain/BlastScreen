package com.blastscreen.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF5BA8FF),
    onPrimary = Color.White,
    background = Color(0xFFF7FBFF),
    surface = Color.White,
    onBackground = Color(0xFF0F172A)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8BC3FF),
    background = Color(0xFF09111F),
    surface = Color(0xFF102039),
    onBackground = Color(0xFFE8F1FF)
)

@Composable
fun BlastScreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        content = content
    )
}
