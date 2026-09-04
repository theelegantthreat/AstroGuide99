package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Custom Material 3 Light Color Scheme.
 * Centered around Yellow (Primary), Light Blue (Secondary), and Violet (Tertiary/Accent).
 */
val AstroLightColorScheme = lightColorScheme(
    primary = SolarYellowLight,
    onPrimary = OnSolarYellowLight,
    primaryContainer = SolarYellowLightContainer,
    onPrimaryContainer = OnSolarYellowLightContainer,

    secondary = CelestialBlueLight,
    onSecondary = OnCelestialBlueLight,
    secondaryContainer = CelestialBlueLightContainer,
    onSecondaryContainer = OnCelestialBlueLightContainer,

    tertiary = MysticalVioletLight,
    onTertiary = OnMysticalVioletLight,
    tertiaryContainer = MysticalVioletLightContainer,
    onTertiaryContainer = OnMysticalVioletLightContainer,

    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

/**
 * Eye-strain-free complementary Dark Mode variant.
 */
val AstroDarkColorScheme = darkColorScheme(
    primary = SolarYellowDark,
    onPrimary = OnSolarYellowDark,
    primaryContainer = SolarYellowDarkContainer,
    onPrimaryContainer = OnSolarYellowDarkContainer,

    secondary = CelestialBlueDark,
    onSecondary = OnCelestialBlueDark,
    secondaryContainer = CelestialBlueDarkContainer,
    onSecondaryContainer = OnCelestialBlueDarkContainer,

    tertiary = MysticalVioletDark,
    onTertiary = OnMysticalVioletDark,
    tertiaryContainer = MysticalVioletDarkContainer,
    onTertiaryContainer = OnMysticalVioletDarkContainer,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prioritize custom Yellow-LightBlue-Violet palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AstroDarkColorScheme
        else -> AstroLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
