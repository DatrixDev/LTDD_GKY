package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = White,

    secondary = RedContainer,
    onSecondary = RedPrimary,

    background = GrayBackground,
    onBackground = Black,

    surface = White,
    onSurface = Black,

    outline = GrayBorder,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}