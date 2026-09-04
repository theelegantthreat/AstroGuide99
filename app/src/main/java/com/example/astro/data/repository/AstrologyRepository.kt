package com.example.astro.data.repository

import com.example.astro.data.local.AppDatabase
import com.example.astro.data.local.ChartSerializationHelper
import com.example.astro.data.local.UserPreferencesDataStore
import com.example.astro.engine.AstrologyCalculationEngine
import com.example.astro.model.AstrocartographyLine
import com.example.astro.model.ChartData
import com.example.astro.model.ElectionalData
import com.example.astro.model.HoraryData
import com.example.astro.model.MedicalAstrologyData
import com.example.astro.model.UserBirthProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Offline-First Astrology Repository.
 *
 * Implements strict offline-first data caching strategy:
 * 1. Checks Room database cache first.
 * 2. If absent (or expired per the 1-hour rule for transits), invokes Swiss Ephemeris calculation.
 * 3. Persists computed result to Room database.
 * 4. Emits reactive updates via Coroutine Flows to the ViewModel/UI.
 */
class AstrologyRepository(
    private val database: AppDatabase,
    private val userPrefs: UserPreferencesDataStore,
    private val calculationEngine: AstrologyCalculationEngine = AstrologyCalculationEngine()
) {

    val userProfile: Flow<UserBirthProfile> = userPrefs.userProfileFlow

    suspend fun saveUserProfile(profile: UserBirthProfile) {
        withContext(Dispatchers.IO) {
            userPrefs.saveUserProfile(profile)
        }
    }

    /**
     * Get Natal Chart with Room Database caching.
     */
    fun getNatalChart(profile: UserBirthProfile, forceRefresh: Boolean = false): Flow<ChartData> = flow {
        val cacheId = "natal_${profile.year}_${profile.month}_${profile.day}_${profile.hour24}_${profile.minute}_${profile.latitude}_${profile.longitude}"

        if (!forceRefresh) {
            val cached = database.chartDao().getChartById(cacheId)
            if (cached != null) {
                emit(ChartSerializationHelper.entityToChart(cached))
                return@flow
            }
        }

        // Compute using Swiss Ephemeris Placidus calculation engine
        val calculated = calculationEngine.calculateNatalChart(profile).copy(id = cacheId)

        // Cache in Room database
        val entity = ChartSerializationHelper.chartToEntity(calculated)
        database.chartDao().insertChart(entity)

        emit(calculated)
    }.flowOn(Dispatchers.IO)

    /**
     * Get Progressed Chart with Room Database caching.
     */
    fun getProgressedChart(profile: UserBirthProfile, currentYear: Int = 2026, forceRefresh: Boolean = false): Flow<ChartData> = flow {
        val cacheId = "progressed_${profile.year}_${profile.month}_${profile.day}_${profile.hour24}_to_$currentYear"

        if (!forceRefresh) {
            val cached = database.chartDao().getChartById(cacheId)
            if (cached != null) {
                emit(ChartSerializationHelper.entityToChart(cached))
                return@flow
            }
        }

        val calculated = calculationEngine.calculateProgressedChart(profile, currentYear).copy(id = cacheId)
        val entity = ChartSerializationHelper.chartToEntity(calculated)
        database.chartDao().insertChart(entity)

        emit(calculated)
    }.flowOn(Dispatchers.IO)

    /**
     * Get Transits with explicit 1-Hour Room Database Expiration Rule.
     */
    fun getTransits(profile: UserBirthProfile, forceRefresh: Boolean = false): Flow<ChartData> = flow {
        val now = System.currentTimeMillis()
        val hourBucket = now / 3600000L
        val cacheKey = "transits_${profile.latitude.toInt()}_${profile.longitude.toInt()}_bucket_$hourBucket"

        // Purge any transits older than 1 hour from the cache table
        database.chartDao().purgeExpiredTransits(now)

        if (!forceRefresh) {
            val cached = database.chartDao().getTransitByKey(cacheKey)
            if (cached != null && !cached.isExpired(now)) {
                emit(ChartSerializationHelper.transitEntityToChart(cached))
                return@flow
            }
        }

        // Cache missing or expired: run Swiss Ephemeris engine
        val calculated = calculationEngine.calculateTransits(profile, now).copy(id = cacheKey)

        // Store into Room transit_cache with current timestamp
        val transitEntity = ChartSerializationHelper.chartToTransitEntity(calculated, cacheKey)
        database.chartDao().insertTransit(transitEntity)

        emit(calculated)
    }.flowOn(Dispatchers.IO)

    /**
     * Horary calculation flow.
     */
    fun getHoraryChart(
        question: String,
        lat: Double,
        lon: Double,
        locationName: String
    ): Flow<Pair<ChartData, HoraryData>> = flow {
        val result = calculationEngine.calculateHorary(question, lat, lon, locationName)
        emit(result)
    }.flowOn(Dispatchers.IO)

    /**
     * Electional calculation flow.
     */
    fun getElectionalChart(
        purpose: String,
        targetYear: Int,
        targetMonth: Int,
        targetDay: Int,
        hour: Int,
        minute: Int,
        lat: Double,
        lon: Double,
        locationName: String
    ): Flow<Pair<ChartData, ElectionalData>> = flow {
        val result = calculationEngine.calculateElectional(
            purpose, targetYear, targetMonth, targetDay, hour, minute, lat, lon, locationName
        )
        emit(result)
    }.flowOn(Dispatchers.IO)

    /**
     * Locational (Astrocartography) line coordinates flow.
     */
    fun getAstrocartography(profile: UserBirthProfile): Flow<List<AstrocartographyLine>> = flow {
        val lines = calculationEngine.calculateAstrocartography(profile)
        emit(lines)
    }.flowOn(Dispatchers.IO)

    /**
     * Medical Astrology humoral & planetary analysis flow.
     */
    fun getMedicalAstrology(profile: UserBirthProfile): Flow<MedicalAstrologyData> = flow {
        val data = calculationEngine.calculateMedicalAstrology(profile)
        emit(data)
    }.flowOn(Dispatchers.IO)
}
