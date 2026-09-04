package com.example.astro.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GroundingSource(
    val title: String,
    val url: String
)

data class GeminiOracleResponse(
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val generatedImage: Bitmap? = null
)

/**
 * Gemini AI Astrology Service.
 * Implements:
 * 1. Multi-turn Chatbot with conversation history
 * 2. Search Grounding (googleSearch tool)
 * 3. Maps Grounding (googleMaps tool)
 * 4. Image Generation (astrological talisman art via gemini-2.5-flash-image)
 */
class GeminiAstrologyService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY.ifBlank { "" }

    /**
     * Multi-turn chat conversation with astrological system context.
     */
    suspend fun chatWithOracle(
        history: List<ChatMessage>,
        userMessage: String,
        natalSummary: String
    ): Result<GeminiOracleResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(
                GeminiOracleResponse(
                    text = "Cosmic Oracle Insights (Offline Radix Reading):\n" +
                            "Based on your Placidus natal blueprint ($natalSummary), the celestial spheres indicate strong resilience and creative growth. " +
                            "\n\n[Tip: Configure your GEMINI_API_KEY in the AI Studio Secrets panel to enable real-time Gemini 3.8-flash generative AI consultations, Search Grounding, and Sacred Art synthesis!]"
                )
            )
        }

        try {
            val jsonPayload = JSONObject()

            // System instructions
            val sysInst = JSONObject().apply {
                val parts = JSONArray().put(JSONObject().apply {
                    put("text", "You are the AstroGuide99 Celestial AI Astrologer. You speak with wisdom, precision, and archetypal depth. The user's natal chart details are: $natalSummary. Synthesize your astrological readings directly incorporating their placements.")
                })
                put("parts", parts)
            }
            jsonPayload.put("systemInstruction", sysInst)

            // Conversation history contents
            val contents = JSONArray()
            history.forEach { msg ->
                val contentObj = JSONObject().apply {
                    put("role", msg.role)
                    put("parts", JSONArray().put(JSONObject().apply { put("text", msg.content) }))
                }
                contents.put(contentObj)
            }

            // Current message
            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().apply { put("text", userMessage) }))
            })
            jsonPayload.put("contents", contents)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error ${response.code}: $responseString"))
            }

            val resJson = JSONObject(responseString)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: "The celestial alignment is silent."

            Result.success(GeminiOracleResponse(text = text))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search-Grounded Query using Google Search tool.
     */
    suspend fun queryWithSearchGrounding(query: String): Result<GeminiOracleResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(
                GeminiOracleResponse(
                    text = "Current Celestial Weather & Eclipses:\n" +
                            "Planetary cycles in 2026 feature key nodal shifts across Pisces and Virgo, with Saturn and Neptune transiting late Pisces into Aries. Add GEMINI_API_KEY to search live celestial ephemeris updates with Google Search grounding.",
                    sources = listOf(GroundingSource("Swiss Ephemeris Documentation", "https://www.astro.com/swisseph/"))
                )
            )
        }

        try {
            val jsonPayload = JSONObject()
            val contents = JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply { put("text", query) }))
            })
            jsonPayload.put("contents", contents)

            // Google Search tool grounding
            val tools = JSONArray().put(JSONObject().apply {
                put("googleSearch", JSONObject())
            })
            jsonPayload.put("tools", tools)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Search Grounding Error: $responseString"))
            }

            val resJson = JSONObject(responseString)
            val candidate = resJson.optJSONArray("candidates")?.optJSONObject(0)
            val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            val sources = mutableListOf<GroundingSource>()
            val metadata = candidate?.optJSONObject("groundingMetadata")
            val chunks = metadata?.optJSONArray("groundingChunks")
            if (chunks != null) {
                for (i in 0 until chunks.length()) {
                    val web = chunks.getJSONObject(i).optJSONObject("web")
                    if (web != null) {
                        sources.add(GroundingSource(web.optString("title", "Reference Source"), web.optString("uri", "")))
                    }
                }
            }

            Result.success(GeminiOracleResponse(text = text, sources = sources))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Maps-Grounded Query using Google Maps tool for sacred astrological observatories.
     */
    suspend fun queryWithMapsGrounding(locationQuery: String): Result<GeminiOracleResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.success(
                GeminiOracleResponse(
                    text = "Sacred Astrological & Astronomical Landmarks:\n" +
                            "• Stonehenge (Wiltshire, UK): Ancient Neolithic solar & lunar eclipse alignment megalith.\n" +
                            "• Jantar Mantar (Jaipur, India): 18th-century stone architectural astronomical observatories with Samrat Yantra.\n" +
                            "• Greenwich Royal Observatory (London, UK): Home of the Prime Meridian (0° Longitude) and Greenwich Mean Time.\n" +
                            "• Griffith Observatory (Los Angeles, USA): Historic public astronomy and celestial planetarium.",
                    sources = listOf(GroundingSource("Sacred Astronomy Coordinates", "https://maps.google.com"))
                )
            )
        }

        try {
            val jsonPayload = JSONObject()
            val contents = JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply { put("text", "Find sacred astrological, astronomical, or megalithic observatories near: $locationQuery") }))
            })
            jsonPayload.put("contents", contents)

            val tools = JSONArray().put(JSONObject().apply {
                put("googleMaps", JSONObject())
            })
            jsonPayload.put("tools", tools)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Maps Grounding Error: $responseString"))
            }

            val resJson = JSONObject(responseString)
            val candidate = resJson.optJSONArray("candidates")?.optJSONObject(0)
            val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""

            Result.success(GeminiOracleResponse(text = text))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate Astrological Celestial Art / Talisman using Gemini Image generation model.
     */
    suspend fun generateCelestialArt(prompt: String): Result<Bitmap?> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Add GEMINI_API_KEY in the AI Studio Secrets panel to generate custom celestial art."))
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", "Astrological sacred talisman artwork: $prompt. Detailed sacred geometry, gold leaf, deep celestial blue and violet.")
                    }))
                }))
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"
            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Image Generation Error: $responseString"))
            }

            val resJson = JSONObject(responseString)
            val candidate = resJson.optJSONArray("candidates")?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            var bitmap: Bitmap? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val inlineData = parts.getJSONObject(i).optJSONObject("inlineData")
                    if (inlineData != null) {
                        val base64Data = inlineData.getString("data")
                        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        break
                    }
                }
            }

            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
