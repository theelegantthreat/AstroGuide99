package com.example.astro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching current transits with an explicit 1-hour expiration rule.
 */
@Entity(tableName = "transit_cache")
data class TransitCacheEntity(
    @PrimaryKey val cacheKey: String,
    val title: String,
    val julianDay: Double,
    val dateTimeDisplay: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val ascendant: Double,
    val midheaven: Double,
    val housesJson: String,
    val planetsJson: String,
    val aspectsJson: String,
    val summaryReading: String,
    val cachedAtMillis: Long = System.currentTimeMillis()
) {
    /**
     * Check if the transit cache has expired (older than 1 hour / 3,600,000 ms).
     */
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return (nowMillis - cachedAtMillis) > 3600000L
    }
}
