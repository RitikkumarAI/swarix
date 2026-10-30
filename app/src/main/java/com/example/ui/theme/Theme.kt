package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class SwarixExtraColors(
    val mainBackground: Color,
    val secondaryBackground: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val divider: Color,
    val error: Color,
    val success: Color
)

val LocalSwarixColors = staticCompositionLocalOf {
    SwarixExtraColors(
        mainBackground = DarkMainBackground,
        secondaryBackground = DarkSecondaryBackground,
        surface = DarkSurface,
        elevatedSurface = DarkElevatedSurface,
        primaryText = DarkPrimaryText,
        secondaryText = DarkSecondaryText,
        mutedText = DarkMutedText,
        primaryAccent = DarkPrimaryAccent,
        secondaryAccent = DarkSecondaryAccent,
        divider = DarkDivider,
        error = DarkError,
        success = DarkSuccess
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryAccent,
    onPrimary = DarkMainBackground,
    primaryContainer = DarkElevatedSurface,
    onPrimaryContainer = DarkPrimaryText,
    secondary = DarkSecondaryAccent,
    onSecondary = DarkMainBackground,
    background = DarkMainBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkDivider,
    outlineVariant = DarkDivider,
    error = DarkError,
    onError = DarkPrimaryText
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryAccent,
    onPrimary = Color.White,
    primaryContainer = LightSecondaryBackground,
    onPrimaryContainer = LightPrimaryText,
    secondary = LightSecondaryAccent,
    onSecondary = Color.White,
    background = LightMainBackground,
    onBackground = LightPrimaryText,
    surface = LightSurface,
    onSurface = LightPrimaryText,
    surfaceVariant = LightElevatedSurface,
    onSurfaceVariant = LightSecondaryText,
    outline = LightDivider,
    outlineVariant = LightDivider,
    error = LightError,
    onError = Color.White
)

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

@Composable
fun SwarixTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extraColors = if (darkTheme) {
        SwarixExtraColors(
            mainBackground = DarkMainBackground,
            secondaryBackground = DarkSecondaryBackground,
            surface = DarkSurface,
            elevatedSurface = DarkElevatedSurface,
            primaryText = DarkPrimaryText,
            secondaryText = DarkSecondaryText,
            mutedText = DarkMutedText,
            primaryAccent = DarkPrimaryAccent,
            secondaryAccent = DarkSecondaryAccent,
            divider = DarkDivider,
            error = DarkError,
            success = DarkSuccess
        )
    } else {
        SwarixExtraColors(
            mainBackground = LightMainBackground,
            secondaryBackground = LightSecondaryBackground,
            surface = LightSurface,
            elevatedSurface = LightElevatedSurface,
            primaryText = LightPrimaryText,
            secondaryText = LightSecondaryText,
            mutedText = LightMutedText,
            primaryAccent = LightPrimaryAccent,
            secondaryAccent = LightSecondaryAccent,
            divider = LightDivider,
            error = LightError,
            success = LightSuccess
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = extraColors.mainBackground.toArgb()
                window.navigationBarColor = extraColors.mainBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalSwarixColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
