package com.example.chronolens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults


private val DarkColorScheme = darkColorScheme(
    primary = dark_red,
    secondary = dark_blue,
    tertiary = light_blue,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.White,
    onSecondary = medium_gray,
    onTertiary = medium_gray_text,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = buttonPrimary,
    onPrimaryContainer = buttonOnPrimary,
    secondaryContainer = buttonSecondary,
    onSecondaryContainer = buttonOnSecondary
)


@Composable
fun defaultButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.secondaryContainer
)


@Composable
fun ChronoLensTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}