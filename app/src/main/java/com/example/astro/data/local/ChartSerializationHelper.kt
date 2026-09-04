package com.example.astro.data.local

import com.example.astro.data.local.entity.CalculatedChartEntity
import com.example.astro.data.local.entity.TransitCacheEntity
import com.example.astro.model.Aspect
import com.example.astro.model.AspectType
import com.example.astro.model.ChartData
import com.example.astro.model.ChartType
import com.example.astro.model.HouseCusp
import com.example.astro.model.Planet
import com.example.astro.model.PlanetaryPlacement
import com.example.astro.model.ZodiacSign
import org.json.JSONArray
import org.json.JSONObject

/**
 * High-performance, zero-dependency JSON serialization for Room chart caching.
 */
object ChartSerializationHelper {

    fun chartToEntity(chart: ChartData): CalculatedChartEntity {
        return CalculatedChartEntity(
            id = chart.id,
            chartType = chart.chartType.name,
            title = chart.title,
            julianDay = chart.julianDay,
            dateTimeDisplay = chart.dateTimeDisplay,
            locationName = chart.locationName,
            latitude = chart.latitude,
            longitude = chart.longitude,
            ascendant = chart.ascendant,
            midheaven = chart.midheaven,
            housesJson = serializeHouses(chart.houses),
            planetsJson = serializePlanets(chart.planets),
            aspectsJson = serializeAspects(chart.aspects),
            summaryReading = chart.summaryReading,
            cachedAtMillis = System.currentTimeMillis()
        )
    }

    fun chartToTransitEntity(chart: ChartData, cacheKey: String): TransitCacheEntity {
        return TransitCacheEntity(
            cacheKey = cacheKey,
            title = chart.title,
            julianDay = chart.julianDay,
            dateTimeDisplay = chart.dateTimeDisplay,
            locationName = chart.locationName,
            latitude = chart.latitude,
            longitude = chart.longitude,
            ascendant = chart.ascendant,
            midheaven = chart.midheaven,
            housesJson = serializeHouses(chart.houses),
            planetsJson = serializePlanets(chart.planets),
            aspectsJson = serializeAspects(chart.aspects),
            summaryReading = chart.summaryReading,
            cachedAtMillis = System.currentTimeMillis()
        )
    }

    fun entityToChart(entity: CalculatedChartEntity): ChartData {
        val chartType = try { ChartType.valueOf(entity.chartType) } catch (e: Exception) { ChartType.NATAL }
        return ChartData(
            id = entity.id,
            title = entity.title,
            chartType = chartType,
            julianDay = entity.julianDay,
            dateTimeDisplay = entity.dateTimeDisplay,
            locationName = entity.locationName,
            latitude = entity.latitude,
            longitude = entity.longitude,
            ascendant = entity.ascendant,
            midheaven = entity.midheaven,
            houses = deserializeHouses(entity.housesJson),
            planets = deserializePlanets(entity.planetsJson),
            aspects = deserializeAspects(entity.aspectsJson),
            summaryReading = entity.summaryReading
        )
    }

    fun transitEntityToChart(entity: TransitCacheEntity): ChartData {
        return ChartData(
            id = entity.cacheKey,
            title = entity.title,
            chartType = ChartType.TRANSITS,
            julianDay = entity.julianDay,
            dateTimeDisplay = entity.dateTimeDisplay,
            locationName = entity.locationName,
            latitude = entity.latitude,
            longitude = entity.longitude,
            ascendant = entity.ascendant,
            midheaven = entity.midheaven,
            houses = deserializeHouses(entity.housesJson),
            planets = deserializePlanets(entity.planetsJson),
            aspects = deserializeAspects(entity.aspectsJson),
            summaryReading = entity.summaryReading
        )
    }

    private fun serializeHouses(houses: List<HouseCusp>): String {
        val arr = JSONArray()
        houses.forEach { h ->
            val obj = JSONObject()
            obj.put("n", h.number)
            obj.put("lon", h.longitude)
            obj.put("s", h.sign.name)
            obj.put("deg", h.degreeInSign)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun deserializeHouses(json: String): List<HouseCusp> {
        val list = mutableListOf<HouseCusp>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val sign = ZodiacSign.valueOf(obj.getString("s"))
                list.add(HouseCusp(obj.getInt("n"), obj.getDouble("lon"), sign, obj.getDouble("deg")))
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    private fun serializePlanets(planets: List<PlanetaryPlacement>): String {
        val arr = JSONArray()
        planets.forEach { p ->
            val obj = JSONObject()
            obj.put("p", p.planet.name)
            obj.put("lon", p.longitude)
            obj.put("lat", p.latitude)
            obj.put("spd", p.speed)
            obj.put("s", p.sign.name)
            obj.put("deg", p.degreeInSign)
            obj.put("h", p.house)
            obj.put("r", p.isRetrograde)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun deserializePlanets(json: String): List<PlanetaryPlacement> {
        val list = mutableListOf<PlanetaryPlacement>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val planet = Planet.valueOf(obj.getString("p"))
                val sign = ZodiacSign.valueOf(obj.getString("s"))
                list.add(
                    PlanetaryPlacement(
                        planet = planet,
                        longitude = obj.getDouble("lon"),
                        latitude = obj.optDouble("lat", 0.0),
                        speed = obj.optDouble("spd", 0.0),
                        sign = sign,
                        degreeInSign = obj.getDouble("deg"),
                        house = obj.getInt("h"),
                        isRetrograde = obj.optBoolean("r", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    private fun serializeAspects(aspects: List<Aspect>): String {
        val arr = JSONArray()
        aspects.forEach { a ->
            val obj = JSONObject()
            obj.put("p1", a.planet1.name)
            obj.put("p2", a.planet2.name)
            obj.put("type", a.type.name)
            obj.put("angle", a.exactAngle)
            obj.put("orb", a.orb)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun deserializeAspects(json: String): List<Aspect> {
        val list = mutableListOf<Aspect>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val p1 = Planet.valueOf(obj.getString("p1"))
                val p2 = Planet.valueOf(obj.getString("p2"))
                val type = AspectType.valueOf(obj.getString("type"))
                list.add(Aspect(p1, p2, type, obj.getDouble("angle"), obj.getDouble("orb")))
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }
}
