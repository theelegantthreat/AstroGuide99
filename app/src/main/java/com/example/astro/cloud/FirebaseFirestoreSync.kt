package com.example.astro.cloud

import android.util.Log
import com.example.astro.model.ChartData
import com.example.astro.model.UserBirthProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firebase Firestore Cloud Synchronization Helper.
 * Synchronizes user profiles and calculated charts to Firestore.
 */
class FirebaseFirestoreSync {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun syncProfileToCloud(profile: UserBirthProfile): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        try {
            val profileMap = hashMapOf(
                "fullName" to profile.fullName,
                "year" to profile.year,
                "month" to profile.month,
                "day" to profile.day,
                "hour" to profile.hour,
                "minute" to profile.minute,
                "isAm" to profile.isAm,
                "cityName" to profile.cityName,
                "latitude" to profile.latitude,
                "longitude" to profile.longitude,
                "timeZoneId" to profile.timeZoneId,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid)
                .collection("profiles").document("primary")
                .set(profileMap).await()
            true
        } catch (e: Exception) {
            Log.w("FirebaseFirestoreSync", "Firestore sync failed or offline: ${e.message}")
            false
        }
    }

    suspend fun saveChartToCloud(chart: ChartData): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        try {
            val chartMap = hashMapOf(
                "id" to chart.id,
                "title" to chart.title,
                "chartType" to chart.chartType.name,
                "julianDay" to chart.julianDay,
                "dateTimeDisplay" to chart.dateTimeDisplay,
                "locationName" to chart.locationName,
                "ascendant" to chart.ascendant,
                "midheaven" to chart.midheaven,
                "summaryReading" to chart.summaryReading,
                "uploadedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid)
                .collection("charts").document(chart.id)
                .set(chartMap).await()
            true
        } catch (e: Exception) {
            Log.w("FirebaseFirestoreSync", "Firestore save chart failed or offline: ${e.message}")
            false
        }
    }
}
