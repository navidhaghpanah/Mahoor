package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = MahoorPrimary,
    secondary = MahoorSecondary,
    background = MahoorDarkBg,
    surface = MahoorSurface,
    surfaceVariant = MahoorSurfaceVariant,
    onPrimary = MahoorOnPrimary,
    onSecondary = MahoorOnSecondary,
    onBackground = MahoorOnBackground,
    onSurface = MahoorOnSurface,
    onSurfaceVariant = MahoorOnBackground
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MahoorPrimary,
    secondary = MahoorSecondary,
    background = MahoorDarkBg, // Force a consistent premium dark aesthetic
    surface = MahoorSurface,
    surfaceVariant = MahoorSurfaceVariant,
    onPrimary = MahoorOnPrimary,
    onSecondary = MahoorOnSecondary,
    onBackground = MahoorOnBackground,
    onSurface = MahoorOnSurface,
    onSurfaceVariant = MahoorOnBackground
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default for elite premium agent workspace
  dynamicColor: Boolean = false, // Disable dynamic color to maintain strict brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
