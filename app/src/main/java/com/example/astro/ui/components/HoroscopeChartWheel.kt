package com.example.astro.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.astro.model.AspectType
import com.example.astro.model.ChartData
import com.example.astro.model.ZodiacSign
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Jetpack Compose Canvas component for rendering a 12-House Placidus Horoscope Wheel.
 * Utilizes drawCircle, drawLine, and nativeCanvas drawText for planetary glyph coordinates.
 */
@Composable
fun HoroscopeChartWheel(
    chartData: ChartData,
    modifier: Modifier = Modifier,
    highlightPlanet: String? = null
) {
    val isDark = MaterialTheme.colorScheme.background.toArgb() != 0xFFFCF8F2.toInt()
    val wheelBackgroundColor = MaterialTheme.colorScheme.surface
    val wheelBorderColor = MaterialTheme.colorScheme.outline
    val primaryGold = MaterialTheme.colorScheme.primary
    val secondaryBlue = MaterialTheme.colorScheme.secondary
    val tertiaryViolet = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .background(wheelBackgroundColor)
            .padding(8.dp)
            .testTag("horoscope_chart_wheel")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width * 0.47f
            val signInnerRadius = size.width * 0.38f
            val planetRadius = size.width * 0.29f
            val aspectRadius = size.width * 0.20f

            val asc = chartData.ascendant

            // 1. Artistic Flair Outer Halo & Concentric boundary circles
            // Celestial Blue outer halo ring (#60A5FA with soft opacity)
            drawCircle(
                color = secondaryBlue.copy(alpha = 0.12f),
                radius = outerRadius + 4.dp.toPx(),
                center = center,
                style = Stroke(width = 10.dp.toPx())
            )
            // Outer slate ring
            drawCircle(
                color = wheelBorderColor.copy(alpha = 0.7f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Background disc
            drawCircle(
                color = wheelBackgroundColor,
                radius = outerRadius - 1.dp.toPx(),
                center = center
            )
            drawCircle(
                color = wheelBorderColor.copy(alpha = 0.45f),
                radius = signInnerRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = primaryGold.copy(alpha = 0.35f),
                radius = planetRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            // Translucent inner hub (aspect disc)
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = aspectRadius,
                center = center
            )
            drawCircle(
                color = wheelBorderColor.copy(alpha = 0.4f),
                radius = aspectRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Native Paint for crisp text & glyph rendering
            val glyphPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = outerRadius * 0.11f
                typeface = Typeface.DEFAULT_BOLD
                color = textColor.toArgb()
            }

            val signPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = outerRadius * 0.12f
                typeface = Typeface.DEFAULT_BOLD
                color = primaryGold.toArgb()
            }

            val smallLabelPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = outerRadius * 0.065f
                color = secondaryBlue.toArgb()
            }

            // 2. Draw 12 Zodiac Segments (30 degrees each)
            val zodiacSigns = ZodiacSign.entries
            for (i in 0 until 12) {
                val startLon = i * 30.0
                val angleStart = (180.0 - (startLon - asc)) * (PI / 180.0)
                val p1 = Offset(
                    (center.x + signInnerRadius * cos(angleStart)).toFloat(),
                    (center.y - signInnerRadius * sin(angleStart)).toFloat()
                )
                val p2 = Offset(
                    (center.x + outerRadius * cos(angleStart)).toFloat(),
                    (center.y - outerRadius * sin(angleStart)).toFloat()
                )
                drawLine(
                    color = wheelBorderColor.copy(alpha = 0.35f),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.dp.toPx()
                )

                // Zodiac Glyph in segment midpoint
                val midLon = startLon + 15.0
                val angleMid = (180.0 - (midLon - asc)) * (PI / 180.0)
                val signMidRadius = (outerRadius + signInnerRadius) / 2f
                val glyphX = (center.x + signMidRadius * cos(angleMid)).toFloat()
                val glyphY = (center.y - signMidRadius * sin(angleMid)).toFloat() + (signPaint.textSize / 3f)

                drawContext.canvas.nativeCanvas.drawText(
                    zodiacSigns[i].symbol,
                    glyphX,
                    glyphY,
                    signPaint
                )
            }

            // 3. Draw 12 Placidus House Cusps
            val cusps = chartData.houses
            cusps.forEach { cusp ->
                val houseAngle = (180.0 - (cusp.longitude - asc)) * (PI / 180.0)
                val isCardinal = cusp.number in listOf(1, 4, 7, 10)
                val cuspStroke = if (isCardinal) 2.5.dp.toPx() else 1.dp.toPx()
                val cuspColor = if (isCardinal) primaryGold else wheelBorderColor.copy(alpha = 0.4f)

                val cuspStart = Offset(
                    (center.x + aspectRadius * cos(houseAngle)).toFloat(),
                    (center.y - aspectRadius * sin(houseAngle)).toFloat()
                )
                val cuspEnd = Offset(
                    (center.x + signInnerRadius * cos(houseAngle)).toFloat(),
                    (center.y - signInnerRadius * sin(houseAngle)).toFloat()
                )

                drawLine(
                    color = cuspColor,
                    start = cuspStart,
                    end = cuspEnd,
                    strokeWidth = cuspStroke
                )

                // Draw House Numbers near the aspect circle
                val labelRadius = aspectRadius + (outerRadius * 0.07f)
                val nextCuspLon = cusps.getOrNull(cusp.number % 12)?.longitude ?: (cusp.longitude + 30.0)
                val midHouseLon = (cusp.longitude + 15.0)
                val midHouseAngle = (180.0 - (midHouseLon - asc)) * (PI / 180.0)
                val numX = (center.x + labelRadius * cos(midHouseAngle)).toFloat()
                val numY = (center.y - labelRadius * sin(midHouseAngle)).toFloat() + (smallLabelPaint.textSize / 3f)

                drawContext.canvas.nativeCanvas.drawText(
                    "${cusp.number}",
                    numX,
                    numY,
                    smallLabelPaint
                )
            }

            // 4. Draw Aspect Lines in center
            chartData.aspects.forEach { aspect ->
                val p1Lon = chartData.planets.firstOrNull { it.planet == aspect.planet1 }?.longitude ?: 0.0
                val p2Lon = chartData.planets.firstOrNull { it.planet == aspect.planet2 }?.longitude ?: 0.0

                val ang1 = (180.0 - (p1Lon - asc)) * (PI / 180.0)
                val ang2 = (180.0 - (p2Lon - asc)) * (PI / 180.0)

                val pt1 = Offset(
                    (center.x + aspectRadius * cos(ang1)).toFloat(),
                    (center.y - aspectRadius * sin(ang1)).toFloat()
                )
                val pt2 = Offset(
                    (center.x + aspectRadius * cos(ang2)).toFloat(),
                    (center.y - aspectRadius * sin(ang2)).toFloat()
                )

                val aspectColor = when (aspect.type) {
                    AspectType.TRINE, AspectType.SEXTILE -> secondaryBlue.copy(alpha = 0.55f)
                    AspectType.SQUARE, AspectType.OPPOSITION -> tertiaryViolet.copy(alpha = 0.65f)
                    AspectType.CONJUNCTION -> primaryGold.copy(alpha = 0.7f)
                }

                drawLine(
                    color = aspectColor,
                    start = pt1,
                    end = pt2,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Artistic Flair Centerpiece: Sacred Violet glowing jewel
            drawCircle(
                color = tertiaryViolet.copy(alpha = 0.25f),
                radius = 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = tertiaryViolet,
                radius = 5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = center
            )

            // 5. Plot Planetary Glyphs & Coordinates
            val planetPosTrackRadius = (signInnerRadius + planetRadius) / 2f
            chartData.planets.forEach { placement ->
                val pAngle = (180.0 - (placement.longitude - asc)) * (PI / 180.0)
                val px = (center.x + planetPosTrackRadius * cos(pAngle)).toFloat()
                val py = (center.y - planetPosTrackRadius * sin(pAngle)).toFloat()

                val isHighlighted = placement.planet.planetName.equals(highlightPlanet, ignoreCase = true)
                glyphPaint.color = if (isHighlighted) primaryGold.toArgb() else textColor.toArgb()

                // Small tick from circle
                val tickStart = Offset(
                    (center.x + signInnerRadius * cos(pAngle)).toFloat(),
                    (center.y - signInnerRadius * sin(pAngle)).toFloat()
                )
                val tickEnd = Offset(
                    (center.x + (signInnerRadius - 10f) * cos(pAngle)).toFloat(),
                    (center.y - (signInnerRadius - 10f) * sin(pAngle)).toFloat()
                )
                drawLine(
                    color = primaryGold,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = 1.5.dp.toPx()
                )

                // Glyph
                drawContext.canvas.nativeCanvas.drawText(
                    placement.planet.glyph,
                    px,
                    py + (glyphPaint.textSize / 3f),
                    glyphPaint
                )

                // Degree text and Retrograde indicator
                val retroText = if (placement.isRetrograde) "${placement.degreeInSign.toInt()}°℞" else "${placement.degreeInSign.toInt()}°"
                val degRadius = planetRadius - (outerRadius * 0.05f)
                val degX = (center.x + degRadius * cos(pAngle)).toFloat()
                val degY = (center.y - degRadius * sin(pAngle)).toFloat() + (smallLabelPaint.textSize / 3f)

                drawContext.canvas.nativeCanvas.drawText(
                    retroText,
                    degX,
                    degY,
                    smallLabelPaint
                )
            }

            // 6. Cardinal Axis Labels: AC, DC, MC, IC
            val acPoint = Offset(center.x - outerRadius, center.y)
            val dcPoint = Offset(center.x + outerRadius, center.y)
            val mcAngle = (180.0 - (chartData.midheaven - asc)) * (PI / 180.0)
            val mcPoint = Offset(
                (center.x + outerRadius * cos(mcAngle)).toFloat(),
                (center.y - outerRadius * sin(mcAngle)).toFloat()
            )

            val axisPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = outerRadius * 0.08f
                typeface = Typeface.DEFAULT_BOLD
                color = primaryGold.toArgb()
            }

            drawContext.canvas.nativeCanvas.drawText("AC", acPoint.x + 20f, acPoint.y - 10f, axisPaint)
            drawContext.canvas.nativeCanvas.drawText("DC", dcPoint.x - 20f, dcPoint.y - 10f, axisPaint)
            drawContext.canvas.nativeCanvas.drawText("MC", mcPoint.x, mcPoint.y - 10f, axisPaint)
        }
    }
}
