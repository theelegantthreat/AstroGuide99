package com.example.astro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.astro.data.local.entity.CalculatedChartEntity
import com.example.astro.data.local.entity.TransitCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChartDao {

    @Query("SELECT * FROM calculated_charts WHERE id = :id LIMIT 1")
    suspend fun getChartById(id: String): CalculatedChartEntity?

    @Query("SELECT * FROM calculated_charts WHERE chartType = :type ORDER BY cachedAtMillis DESC")
    fun getChartsByType(type: String): Flow<List<CalculatedChartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chart: CalculatedChartEntity)

    @Query("DELETE FROM calculated_charts WHERE id = :id")
    suspend fun deleteChartById(id: String)

    // Transit Caching with 1-hour expiration support
    @Query("SELECT * FROM transit_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getTransitByKey(key: String): TransitCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransit(transit: TransitCacheEntity)

    @Query("DELETE FROM transit_cache WHERE (:nowMillis - cachedAtMillis) > 3600000")
    suspend fun purgeExpiredTransits(nowMillis: Long = System.currentTimeMillis())
}
