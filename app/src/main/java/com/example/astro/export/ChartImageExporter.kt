package com.example.astro.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.astro.model.ChartData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Image Export Utility using Scoped Storage (MediaStore API).
 * Saves uncompressed PNGs to Pictures/AstroGuide99 and launches sharing intents.
 */
object ChartImageExporter {

    suspend fun exportAndShareChartPng(
        context: Context,
        chartData: ChartData,
        shareImmediately: Boolean = true
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = ChartBitmapRenderer.renderChartWheelBitmap(chartData, sizePx = 1400)
            val fileName = "AstroGuide99_${chartData.id}_${System.currentTimeMillis()}.png"

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AstroGuide99")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { outStream: OutputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                imageUri
            } else {
                // Fallback for older versions or external cache directory with FileProvider
                val cacheDir = File(context.externalCacheDir ?: context.cacheDir, "shared_charts")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val imageFile = File(cacheDir, fileName)
                FileOutputStream(imageFile).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
            }

            if (uri != null && shareImmediately) {
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "${chartData.title} - AstroGuide99")
                        putExtra(Intent.EXTRA_TEXT, "Astrological Chart generated with AstroGuide99 using Swiss Ephemeris Placidus Houses: ${chartData.title}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Share Astrology Chart")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                    Toast.makeText(context, "Chart image exported to Pictures!", Toast.LENGTH_SHORT).show()
                }
            }

            uri
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export image: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }
}
