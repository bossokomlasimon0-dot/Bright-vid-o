package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrightVioletPrimaryLight,
    onPrimary = Color(0xFF1E1035),
    primaryContainer = BrightVioletPrimaryDark,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = BrightAmberLight,
    onSecondary = Color(0xFF452200),
    secondaryContainer = BrightAmberDark,
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = BrightCyan,
    onTertiary = Color(0xFF00363D),
    background = BrightDarkBackground,
    onBackground = TextPrimaryDark,
    surface = BrightDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = BrightDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BrightDarkOutline,
    error = StatusRejectedRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrightVioletPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = BrightLightSurfaceVariant,
    onPrimaryContainer = BrightVioletPrimaryDark,
    secondary = BrightAmberDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF452200),
    tertiary = BrightCyan,
    onTertiary = Color.White,
    background = BrightLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = BrightLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = BrightLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = BrightLightOutline,
    error = StatusRejectedRed,
    onError = Color.White
)

@Composable
fun BrightVideoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand colors for cohesive identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
