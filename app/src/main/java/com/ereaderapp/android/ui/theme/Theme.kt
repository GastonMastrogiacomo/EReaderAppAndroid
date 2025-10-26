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
    primary = PrimaryBlue,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = PrimaryBlueDark,

    secondary = SecondaryGray,
    onSecondary = TextOnPrimary,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = SecondaryGrayDark,

    tertiary = AccentYellow,
    onTertiary = TextPrimary,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = AccentRed,
    onError = TextOnPrimary,
    errorContainer = Color(0xFFFFF3F3),
    onErrorContainer = AccentRed,

    outline = SurfaceBorder,
    outlineVariant = SecondaryGrayLight,

    scrim = OverlayMedium,

    inverseSurface = SurfaceDark,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryBlueLight,

    surfaceTint = PrimaryBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDarkTheme,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,

    secondary = SecondaryGrayLight,
    onSecondary = Color.White,
    secondaryContainer = SecondaryGray,
    onSecondaryContainer = SecondaryGrayLight,

    tertiary = AccentYellow,
    onTertiary = TextPrimary,

    background = BackgroundDark,
    onBackground = Color.White,

    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = SecondaryGrayLight,

    error = AccentRed,
    onError = Color.White,
    errorContainer = Color(0xFF5F1A1A),
    onErrorContainer = Color(0xFFFFBFBF),

    outline = SecondaryGrayDark,
    outlineVariant = SecondaryGray,

    scrim = OverlayDark,

    inverseSurface = SurfaceLight,
    inverseOnSurface = TextPrimary,
    inversePrimary = PrimaryBlue,

    surfaceTint = PrimaryBlueDarkTheme
)

@Composable
fun EReaderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Cambiado a false para usar nuestros colores personalizados
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




