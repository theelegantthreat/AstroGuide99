package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Artistic Flair Palette
// ==========================================

// Light Mode (Warm Ivory Canvas, Radiant Amber Gold, Sacred Violet, Celestial Blue)
val ArtisticCanvasIvory = Color(0xFFFFFDF0)       // #FFFDF0
val ArtisticTextCharcoal = Color(0xFF1C1B1F)      // #1C1B1F
val ArtisticTextSlate = Color(0xFF1E293B)         // Slate 800 #1E293B
val ArtisticMutedSlate = Color(0xFF64748B)        // Slate 500 #64748B
val ArtisticSubtleSlate = Color(0xFF94A3B8)       // Slate 400 #94A3B8

val ArtisticAmberGold = Color(0xFFFBBF24)         // Primary #FBBF24
val ArtisticAmberDark = Color(0xFFF59E0B)         // Amber 600 #F59E0B
val ArtisticAmberContainer = Color(0xFFFEF3C7)    // Amber 100 #FEF3C7
val ArtisticOnAmberContainer = Color(0xFF78350F)  // Amber 900 #78350F

val ArtisticSkyBlue = Color(0xFF60A5FA)           // Blue 400 #60A5FA
val ArtisticSkyBlueDark = Color(0xFF3B82F6)       // Blue 500 #3B82F6
val ArtisticSkyBlueContainer = Color(0xFFEFF6FF)  // Blue 50 #EFF6FF
val ArtisticOnSkyBlueContainer = Color(0xFF1E40AF)// Blue 800 #1E40AF

val ArtisticSacredViolet = Color(0xFF7C3AED)      // Violet 600 #7C3AED
val ArtisticVioletDark = Color(0xFF6D28D9)        // Violet 700 #6D28D9
val ArtisticVioletContainer = Color(0xFFEDE9FE)    // Violet 100 #EDE9FE
val ArtisticOnVioletContainer = Color(0xFF5B21B6)  // Violet 800 #5B21B6

val ArtisticSurfaceWhite = Color(0xFFFFFFFF)      // Pure White Card Surface
val ArtisticSurfaceBorder = Color(0xFFF1F5F9)     // Slate 100 border
val ArtisticBorderSubtle = Color(0xFFE2E8F0)      // Slate 200 border

// Backward-compatible alias tokens for existing code
val SolarYellowLight = ArtisticAmberGold
val SolarYellowLightContainer = ArtisticAmberContainer
val OnSolarYellowLight = ArtisticTextCharcoal
val OnSolarYellowLightContainer = ArtisticOnAmberContainer

val CelestialBlueLight = ArtisticSkyBlueDark
val CelestialBlueLightContainer = ArtisticSkyBlueContainer
val OnCelestialBlueLight = Color(0xFFFFFFFF)
val OnCelestialBlueLightContainer = ArtisticOnSkyBlueContainer

val MysticalVioletLight = ArtisticSacredViolet
val MysticalVioletLightContainer = ArtisticVioletContainer
val OnMysticalVioletLight = Color(0xFFFFFFFF)
val OnMysticalVioletLightContainer = ArtisticOnVioletContainer

val BackgroundLight = ArtisticCanvasIvory
val OnBackgroundLight = ArtisticTextCharcoal
val SurfaceLight = ArtisticSurfaceWhite
val OnSurfaceLight = ArtisticTextCharcoal
val SurfaceVariantLight = ArtisticVioletContainer.copy(alpha = 0.4f)
val OnSurfaceVariantLight = ArtisticTextSlate
val OutlineLight = ArtisticBorderSubtle

// Dark Mode (Deep Ink Canvas with Luminous Accents)
val SolarYellowDark = Color(0xFFFCD34D)
val SolarYellowDarkContainer = Color(0xFF78350F)
val OnSolarYellowDark = Color(0xFF1C1917)
val OnSolarYellowDarkContainer = Color(0xFFFEF3C7)

val CelestialBlueDark = Color(0xFF93C5FD)
val CelestialBlueDarkContainer = Color(0xFF1E3A8A)
val OnCelestialBlueDark = Color(0xFF0F172A)
val OnCelestialBlueDarkContainer = Color(0xFFDBEAFE)

val MysticalVioletDark = Color(0xFFA78BFA)
val MysticalVioletDarkContainer = Color(0xFF4C1D95)
val OnMysticalVioletDark = Color(0xFF1E1B4B)
val OnMysticalVioletDarkContainer = Color(0xFFEDE9FE)

val BackgroundDark = Color(0xFF11131F)
val OnBackgroundDark = Color(0xFFF8FAFC)
val SurfaceDark = Color(0xFF181B29)
val OnSurfaceDark = Color(0xFFF8FAFC)
val SurfaceVariantDark = Color(0xFF22263A)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)
val OutlineDark = Color(0xFF475569)

// Astrological Elemental Accents
val ElementFire = Color(0xFFE65100)
val ElementEarth = Color(0xFF2E7D32)
val ElementAir = Color(0xFF0288D1)
val ElementWater = Color(0xFF7B1FA2)
val WheelGold = Color(0xFFFFC107)
