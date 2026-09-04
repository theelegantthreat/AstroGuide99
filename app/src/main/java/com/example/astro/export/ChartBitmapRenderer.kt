package com.example.astro.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.astro.model.AspectType
import com.example.astro.model.ChartData
import com.example.astro.model.ZodiacSign
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-Resolution Native Bitmap Renderer for Horoscope Chart Wheels.
 * Renders high-fidelity uncompressed 1200x1200px charts for MediaStore PNG and PDF exports.
 */
object ChartBitmapRenderer {

    fun renderChartWheelBitmap(
        chartData: ChartData,
        sizePx: Int = 1200,
        isDark: Boolean = false
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette
        val bgColor = if (isDark) Color.parseColor("#0F111E") else Color.parseColor("#FFFFFF")
        val borderColor = if (isDark) Color.parseColor("#918E9F") else Color.parseColor("#7E7569")
        val goldColor = Color.parseColor("#B37B00")
        val blueColor = Color.parseColor("#00658E")
        val violetColor = Color.parseColor("#6750A4")
        val textColor = if (isDark) Color.parseColor("#E5E1E9") else Color.parseColor("#1C1B1A")

        canvas.drawColor(bgColor)

        val center = sizePx / 2f
        val outerRadius = sizePx * 0.46f
        val signInnerRadius = sizePx * 0.38f
        val planetRadius = sizePx * 0.29f
        val aspectRadius = sizePx * 0.20f

        val asc = chartData.ascendant

        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = borderColor
            strokeWidth = 3f
        }

        val goldPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = goldColor
            strokeWidth = 4f
        }

        // Concentric Circles
        canvas.drawCircle(center, center, outerRadius, goldPaint)
        canvas.drawCircle(center, center, signInnerRadius, strokePaint)
        strokePaint.strokeWidth = 2f
        canvas.drawCircle(center, center, planetRadius, strokePaint)
        canvas.drawCircle(center, center, aspectRadius, strokePaint)

        // Text paints
        val signPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.045f
            typeface = Typeface.DEFAULT_BOLD
            color = goldColor
        }

        val glyphPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.042f
            typeface = Typeface.DEFAULT_BOLD
            color = textColor
        }

        val numPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.024f
            color = blueColor
        }

        // Draw 12 Zodiac signs
        val signs = ZodiacSign.entries
        for (i in 0 until 12) {
            val startLon = i * 30.0
            val angStart = (180.0 - (startLon - asc)) * (PI / 180.0)
            val p1X = (center + signInnerRadius * cos(angStart)).toFloat()
            val p1Y = (center - signInnerRadius * sin(angStart)).toFloat()
            val p2X = (center + outerRadius * cos(angStart)).toFloat()
            val p2Y = (center - outerRadius * sin(angStart)).toFloat()
            canvas.drawLine(p1X, p1Y, p2X, p2Y, strokePaint)

            val midLon = startLon + 15.0
            val angMid = (180.0 - (midLon - asc)) * (PI / 180.0)
            val signMidR = (outerRadius + signInnerRadius) / 2f
            val gx = (center + signMidR * cos(angMid)).toFloat()
            val gy = (center - signMidR * sin(angMid)).toFloat() + (signPaint.textSize / 3f)
            canvas.drawText(signs[i].symbol, gx, gy, signPaint)
        }

        // Draw 12 Placidus House Cusps
        chartData.houses.forEach { cusp ->
            val ang = (180.0 - (cusp.longitude - asc)) * (PI / 180.0)
            val isCard = cusp.number in listOf(1, 4, 7, 10)
            val cPaint = if (isCard) goldPaint else strokePaint

            val sX = (center + aspectRadius * cos(ang)).toFloat()
            val sY = (center - aspectRadius * sin(ang)).toFloat()
            val eX = (center + signInnerRadius * cos(ang)).toFloat()
            val eY = (center - signInnerRadius * sin(ang)).toFloat()
            canvas.drawLine(sX, sY, eX, eY, cPaint)

            // House number
            val labelR = aspectRadius + (sizePx * 0.035f)
            val midHA = (180.0 - (cusp.longitude + 15.0 - asc)) * (PI / 180.0)
            val nX = (center + labelR * cos(midHA)).toFloat()
            val nY = (center - labelR * sin(midHA)).toFloat() + (numPaint.textSize / 3f)
            canvas.drawText("${cusp.number}", nX, nY, numPaint)
        }

        // Aspect Lines
        val aspectPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }

        chartData.aspects.forEach { aspect ->
            val p1Lon = chartData.planets.firstOrNull { it.planet == aspect.planet1 }?.longitude ?: 0.0
            val p2Lon = chartData.planets.firstOrNull { it.planet == aspect.planet2 }?.longitude ?: 0.0

            val ang1 = (180.0 - (p1Lon - asc)) * (PI / 180.0)
            val ang2 = (180.0 - (p2Lon - asc)) * (PI / 180.0)

            val x1 = (center + aspectRadius * cos(ang1)).toFloat()
            val y1 = (center - aspectRadius * sin(ang1)).toFloat()
            val x2 = (center + aspectRadius * cos(ang2)).toFloat()
            val y2 = (center - aspectRadius * sin(ang2)).toFloat()

            aspectPaint.color = when (aspect.type) {
                AspectType.TRINE, AspectType.SEXTILE -> blueColor
                AspectType.SQUARE, AspectType.OPPOSITION -> violetColor
                AspectType.CONJUNCTION -> goldColor
            }
            canvas.drawLine(x1, y1, x2, y2, aspectPaint)
        }

        // Planets
        val planetTrackR = (signInnerRadius + planetRadius) / 2f
        chartData.planets.forEach { p ->
            val pAng = (180.0 - (p.longitude - asc)) * (PI / 180.0)
            val px = (center + planetTrackR * cos(pAng)).toFloat()
            val py = (center - planetTrackR * sin(pAng)).toFloat() + (glyphPaint.textSize / 3f)
            canvas.drawText(p.planet.glyph, px, py, glyphPaint)

            // Degree text
            val degR = planetRadius - (sizePx * 0.025f)
            val dx = (center + degR * cos(pAng)).toFloat()
            val dy = (center - degR * sin(pAng)).toFloat() + (numPaint.textSize / 3f)
            val text = if (p.isRetrograde) "${p.degreeInSign.toInt()}°℞" else "${p.degreeInSign.toInt()}°"
            canvas.drawText(text, dx, dy, numPaint)
        }

        // Cardinal marks
        val axisPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textSize = sizePx * 0.03f
            color = goldColor
        }
        canvas.drawText("AC", center - outerRadius + 25f, center - 10f, axisPaint)
        canvas.drawText("DC", center + outerRadius - 55f, center - 10f, axisPaint)

        return bitmap
    }
}
