package com.accessible.spreadsheet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A6B3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA4F5B7),
    onPrimaryContainer = Color(0xFF002110),
    secondary = Color(0xFF4F6353),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E8D4),
    onSecondaryContainer = Color(0xFF0D1F13),
    tertiary = Color(0xFF3A646F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBDEAF7),
    onTertiaryContainer = Color(0xFF001F27),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFDF7),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFCFDF7),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414941),
    outline = Color(0xFF717970),
    outlineVariant = Color(0xFFC1C9BF),
    inverseSurface = Color(0xFF2F312D),
    inverseOnSurface = Color(0xFFEFF1EB),
    inversePrimary = Color(0xFF89D89E),
    surfaceTint = Color(0xFF1A6B3C)
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF89D89E),
    onPrimary = Color(0xFF00391C),
    primaryContainer = Color(0xFF00522C),
    onPrimaryContainer = Color(0xFFA4F5B7),
    secondary = Color(0xFFB6CCB8),
    onSecondary = Color(0xFF223526),
    secondaryContainer = Color(0xFF384B3C),
    onSecondaryContainer = Color(0xFFD2E8D4),
    tertiary = Color(0xFFA2CED9),
    onTertiary = Color(0xFF013640),
    tertiaryContainer = Color(0xFF204D57),
    onTertiaryContainer = Color(0xFFBDEAF7),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C19),
    onBackground = Color(0xFFE2E3DD),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF8B938A),
    outlineVariant = Color(0xFF414941),
    inverseSurface = Color(0xFFE2E3DD),
    inverseOnSurface = Color(0xFF2F312D),
    inversePrimary = Color(0xFF1A6B3C),
    surfaceTint = Color(0xFF89D89E)
)

/**
 * Main theme composable.
 * @param themeMode "system", "light", or "dark"
 * @param useDynamicColor whether to use Material You dynamic colors
 */
@Composable
fun AccessibleSpreadsheetTheme(
    themeMode: String = "system",
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
