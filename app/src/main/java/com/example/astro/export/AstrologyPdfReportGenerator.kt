package com.example.astro.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.astro.model.ChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Native Android PdfDocument generator.
 * Produces multi-page professional astrological reports.
 * Page 1: Brand title page + High-resolution chart wheel.
 * Page 2+: Planetary placement tables, Placidus cusps, aspects & baseline readings.
 */
object AstrologyPdfReportGenerator {

    suspend fun generateAndSharePdfReport(
        context: Context,
        chartData: ChartData
    ) = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()

            // Standard A4 dimensions: 595 x 842 points (72 points/inch)
            val pageWidth = 595
            val pageHeight = 842

            // Colors
            val colorGold = Color.parseColor("#B37B00")
            val colorBlue = Color.parseColor("#00658E")
            val colorViolet = Color.parseColor("#6750A4")
            val colorDark = Color.parseColor("#1A1A1A")
            val colorGray = Color.parseColor("#555555")
            val colorLightBg = Color.parseColor("#FBF8F2")
            val colorBorder = Color.parseColor("#E0D6C3")

            // ==========================================
            // PAGE 1: TITLE & HIGH-RES CHART WHEEL
            // ==========================================
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = pdfDoc.startPage(pageInfo1)
            val canvas1 = page1.canvas

            // Background
            canvas1.drawColor(colorLightBg)

            // Header Banner
            val headerPaint = Paint().apply {
                isAntiAlias = true
                color = colorGold
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas1.drawText("✦ ASTROGUIDE99 ✦", pageWidth / 2f, 50f, headerPaint)

            val subHeaderPaint = Paint().apply {
                isAntiAlias = true
                color = colorBlue
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas1.drawText("OFFLINE-FIRST SWISS EPHEMERIS ASTROLOGICAL REPORT", pageWidth / 2f, 70f, subHeaderPaint)

            // Decorative Divider
            val linePaint = Paint().apply {
                color = colorGold
                strokeWidth = 2f
            }
            canvas1.drawLine(50f, 82f, pageWidth - 50f, 82f, linePaint)

            // Title & Details Box
            val titlePaint = Paint().apply {
                isAntiAlias = true
                color = colorDark
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            canvas1.drawText(chartData.title, pageWidth / 2f, 115f, titlePaint)

            val metaPaint = Paint().apply {
                isAntiAlias = true
                color = colorGray
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas1.drawText("House System: Placidus ('P')  •  Calculation Engine: Swiss Ephemeris", pageWidth / 2f, 135f, metaPaint)
            canvas1.drawText("Date/Time: ${chartData.dateTimeDisplay}", pageWidth / 2f, 150f, metaPaint)
            canvas1.drawText(
                String.format(
                    Locale.US,
                    "Location: %s (Lat: %.4f°, Lon: %.4f°)",
                    chartData.locationName,
                    chartData.latitude,
                    chartData.longitude
                ),
                pageWidth / 2f,
                165f,
                metaPaint
            )

            // High-Resolution Chart Wheel Bitmap
            val chartBitmap = ChartBitmapRenderer.renderChartWheelBitmap(chartData, sizePx = 900, isDark = false)
            val wheelDestRect = Rect(107, 190, 487, 570)
            canvas1.drawBitmap(chartBitmap, null, wheelDestRect, Paint(Paint.FILTER_BITMAP_FLAG))

            // Baseline Summary Card on Page 1
            val summaryBoxPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val summaryBorderPaint = Paint().apply {
                color = colorBorder
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas1.drawRoundRect(50f, 600f, pageWidth - 50f, 770f, 12f, 12f, summaryBoxPaint)
            canvas1.drawRoundRect(50f, 600f, pageWidth - 50f, 770f, 12f, 12f, summaryBorderPaint)

            val summaryTitlePaint = Paint().apply {
                isAntiAlias = true
                color = colorViolet
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas1.drawText("Constitutional Radix Summary", 65f, 625f, summaryTitlePaint)

            val bodyPaint = Paint().apply {
                isAntiAlias = true
                color = colorDark
                textSize = 10f
            }
            val sunSign = chartData.planets.firstOrNull { it.planet.name == "SUN" }
            val moonSign = chartData.planets.firstOrNull { it.planet.name == "MOON" }
            val ascSign = chartData.houses.firstOrNull { it.number == 1 }

            canvas1.drawText("• Ascendant: ${ascSign?.sign?.signName} (${ascSign?.formatted ?: "H1"}) - Physical envelope & orientation", 65f, 650f, bodyPaint)
            canvas1.drawText("• Sun: ${sunSign?.sign?.signName} (${sunSign?.formattedPlacement ?: ""}) in House ${sunSign?.house} - Core vitality & ego destiny", 65f, 672f, bodyPaint)
            canvas1.drawText("• Moon: ${moonSign?.sign?.signName} (${moonSign?.formattedPlacement ?: ""}) in House ${moonSign?.house} - Intuitive nature & subconscious rhythm", 65f, 694f, bodyPaint)
            canvas1.drawText("• Placidus Houses: Exact oblique ascension semi-arcs calculated offline.", 65f, 716f, bodyPaint)
            canvas1.drawText("• Reading: ${chartData.summaryReading.take(130)}...", 65f, 738f, bodyPaint)

            // Footer
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = colorGray
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            canvas1.drawText("Page 1 of 2  •  AstroGuide99 Professional Astrology", pageWidth / 2f, 810f, footerPaint)
            pdfDoc.finishPage(page1)

            // ==========================================
            // PAGE 2: PLANETARY PLACEMENT TABLES & ASPECTS
            // ==========================================
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = pdfDoc.startPage(pageInfo2)
            val canvas2 = page2.canvas

            canvas2.drawColor(Color.WHITE)

            // Page 2 Header
            val p2HeaderPaint = Paint().apply {
                isAntiAlias = true
                color = colorGold
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas2.drawText("Planetary Positions & Placidus House Cusps", 50f, 50f, p2HeaderPaint)

            canvas2.drawLine(50f, 60f, pageWidth - 50f, 60f, linePaint)

            // Table 1: Planets Header
            val thPaint = Paint().apply {
                isAntiAlias = true
                color = colorBlue
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
            }
            var curY = 85f
            canvas2.drawText("PLANET", 55f, curY, thPaint)
            canvas2.drawText("GLYPH", 125f, curY, thPaint)
            canvas2.drawText("SIGN", 175f, curY, thPaint)
            canvas2.drawText("POSITION", 255f, curY, thPaint)
            canvas2.drawText("HOUSE", 345f, curY, thPaint)
            canvas2.drawText("SPEED/DAY", 410f, curY, thPaint)
            canvas2.drawText("MOTION", 485f, curY, thPaint)

            val rowBgPaint = Paint().apply { color = Color.parseColor("#F7F9FB") }
            val tableTextPaint = Paint().apply {
                isAntiAlias = true
                color = colorDark
                textSize = 9.5f
            }

            curY += 8f
            canvas2.drawLine(50f, curY, pageWidth - 50f, curY, linePaint)

            chartData.planets.forEachIndexed { index, p ->
                curY += 20f
                if (index % 2 == 1) {
                    canvas2.drawRect(50f, curY - 14f, pageWidth - 50f, curY + 6f, rowBgPaint)
                }
                canvas2.drawText(p.planet.planetName, 55f, curY, tableTextPaint)
                canvas2.drawText(p.planet.glyph, 135f, curY, tableTextPaint)
                canvas2.drawText("${p.sign.symbol} ${p.sign.signName}", 175f, curY, tableTextPaint)
                canvas2.drawText(p.formattedPlacement, 255f, curY, tableTextPaint)
                canvas2.drawText("H${p.house}", 355f, curY, tableTextPaint)
                canvas2.drawText(String.format(Locale.US, "%.3f°", p.speed), 410f, curY, tableTextPaint)
                val retroStr = if (p.isRetrograde) "℞ Retrograde" else "Direct"
                canvas2.drawText(retroStr, 485f, curY, tableTextPaint)
            }

            // Table 2: Placidus House Cusps
            curY += 35f
            canvas2.drawText("Placidus House Cusps (Ecliptic Longitude)", 50f, curY, p2HeaderPaint)
            curY += 8f
            canvas2.drawLine(50f, curY, pageWidth - 50f, curY, linePaint)

            curY += 18f
            val col1X = 55f
            val col2X = 300f

            for (h in 1..6) {
                val c1 = chartData.houses.getOrNull(h - 1)
                val c2 = chartData.houses.getOrNull(h + 5)
                val str1 = "House $h: ${c1?.formatted ?: ""}"
                val str2 = "House ${h + 6}: ${c2?.formatted ?: ""}"
                canvas2.drawText(str1, col1X, curY, tableTextPaint)
                canvas2.drawText(str2, col2X, curY, tableTextPaint)
                curY += 18f
            }

            // Table 3: Major Aspects
            curY += 25f
            canvas2.drawText("Major Synthesized Planetary Aspects", 50f, curY, p2HeaderPaint)
            curY += 8f
            canvas2.drawLine(50f, curY, pageWidth - 50f, curY, linePaint)

            curY += 18f
            val aspects = chartData.aspects.take(8)
            if (aspects.isEmpty()) {
                canvas2.drawText("No major tight orbs detected under 8°.", 55f, curY, tableTextPaint)
            } else {
                aspects.forEachIndexed { i, a ->
                    val aspectStr = "${a.planet1.planetName} ${a.type.glyph} ${a.type.aspectName} ${a.planet2.planetName} (Exact: ${String.format(Locale.US, "%.1f°", a.exactAngle)}, Orb: ${String.format(Locale.US, "%.2f°", a.orb)})"
                    val ax = if (i % 2 == 0) col1X else col2X
                    canvas2.drawText(aspectStr, ax, curY, tableTextPaint)
                    if (i % 2 == 1 || i == aspects.lastIndex) {
                        curY += 18f
                    }
                }
            }

            // Footer
            canvas2.drawText("Page 2 of 2  •  AstroGuide99 Professional Astrology  •  Generated Offline", pageWidth / 2f, 810f, footerPaint)
            pdfDoc.finishPage(page2)

            // Write PDF to external cache directory
            val cacheDir = File(context.externalCacheDir ?: context.cacheDir, "astrology_reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val pdfFile = File(cacheDir, "AstroGuide99_${chartData.id}.pdf")

            FileOutputStream(pdfFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            // Launch standard Android share sheet
            val pdfUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                    putExtra(Intent.EXTRA_SUBJECT, "AstroGuide99 Report - ${chartData.title}")
                    putExtra(Intent.EXTRA_TEXT, "Detailed Astrological Report compiled with Swiss Ephemeris and Placidus houses.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share Astrological PDF Report")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                Toast.makeText(context, "PDF Report generated and ready to share!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
