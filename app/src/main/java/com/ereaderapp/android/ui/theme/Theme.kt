package com.ereaderapp.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrown,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryBrownContainer,
    onPrimaryContainer = PrimaryBrownHover,

    secondary = TextSecondary,
    onSecondary = TextOnPrimary,
    secondaryContainer = BorderLight,
    onSecondaryContainer = TextPrimary,

    tertiary = AccentYellow,
    onTertiary = TextPrimary,

    background = BackgroundMain,
    onBackground = TextPrimary,

    surface = BackgroundSection,
    onSurface = TextPrimary,
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextSecondary,

    error = AccentRed,
    onError = TextOnPrimary,
    errorContainer = Color(0xFFFFF3F3),
    onErrorContainer = AccentRed,

    outline = BorderLight,
    outlineVariant = Color(0xFFADB5BD),

    scrim = OverlayMedium,

    inverseSurface = SurfaceDark,
    inverseOnSurface = Color.White,
    inversePrimary = AccentBrown,

    surfaceTint = PrimaryBrown
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBrownDarkTheme,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryBrownHover,
    onPrimaryContainer = AccentBrown,

    secondary = Color(0xFFADB5BD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6C757D),
    onSecondaryContainer = Color(0xFFADB5BD),

    tertiary = AccentYellow,
    onTertiary = TextPrimary,

    background = BackgroundDark,
    onBackground = Color.White,

    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFADB5BD),

    error = AccentRed,
    onError = Color.White,
    errorContainer = Color(0xFF5F1A1A),
    onErrorContainer = Color(0xFFFFBFBF),

    outline = Color(0xFF495057),
    outlineVariant = Color(0xFF6C757D),

    scrim = OverlayDark,

    inverseSurface = BackgroundSection,
    inverseOnSurface = TextPrimary,
    inversePrimary = PrimaryBrown,

    surfaceTint = PrimaryBrownDarkTheme
)

@Composable
fun EReaderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Changed to false to use custom brown colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}