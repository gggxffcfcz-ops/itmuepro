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
    primary = SleekBlueDark,
    secondary = SleekBlueDark,
    tertiary = SleekUrgent,
    background = SleekBgDark,
    surface = SleekSurfaceDark,
    onPrimary = SleekBgDark,
    onSecondary = SleekBgDark,
    onBackground = SleekSurface,
    onSurface = SleekSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekBlue,
    secondary = SleekBlue,
    tertiary = SleekUrgent,
    background = SleekBgLight,
    surface = SleekSurface,
    onPrimary = SleekSurface,
    onSecondary = SleekSurface,
    onBackground = SleekDarkText,
    onSurface = SleekDarkText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to ensure consistent themed look of Sleek Interface
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
