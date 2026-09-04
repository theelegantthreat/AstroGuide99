package com.example.astro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching calculated astrological charts (Natal, Progressed, etc.)
 */
@Entity(tableName = "calculated_charts")
data class CalculatedChartEntity(
    @PrimaryKey val id: String,
    val chartType: String,
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
)
