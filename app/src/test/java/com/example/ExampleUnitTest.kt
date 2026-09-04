package com.example

import com.example.astro.data.local.ChartSerializationHelper
import com.example.astro.data.local.entity.TransitCacheEntity
import com.example.astro.engine.AstrologyCalculationEngine
import com.example.astro.engine.SwissEphemerisConstants
import com.example.astro.engine.SwissEphemerisEngine
import com.example.astro.model.ChartType
import com.example.astro.model.Planet
import com.example.astro.model.UserBirthProfile
import com.example.astro.model.ZodiacSign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    private val ephemeris = SwissEphemerisEngine()
    private val calculationEngine = AstrologyCalculationEngine(ephemeris)

    @Test
    fun testJulianDayCalculation() {
        // J2000 epoch: 2000-01-01 12:00 UT = 2451545.0
        val jd = ephemeris.swe_julday(2000, 1, 1, 12.0)
        assertEquals(2451545.0, jd, 0.0001)
    }

    @Test
    fun testPlacidusHousesCalculation() {
        val jd = ephemeris.swe_julday(1995, 5, 21, 17.5)
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        val res = ephemeris.swe_houses(
            jd, 0, 37.7749, -122.4194, SwissEphemerisConstants.SE_HSYS_PLACIDUS, cusps, ascmc
        )

        assertEquals(SwissEphemerisConstants.OK, res)
        // Ascendant should be within 0..360
        assertTrue(ascmc[0] in 0.0..360.0)
        // Midheaven should be within 0..360
        assertTrue(ascmc[1] in 0.0..360.0)
        // 12 house cusps should be populated
        for (i in 1..12) {
            assertTrue("House $i cusp out of range: ${cusps[i]}", cusps[i] in 0.0..360.0)
        }
    }

    @Test
    fun testPlanetaryPositions() {
        val jd = ephemeris.swe_julday(2026, 9, 4, 12.0)
        val xx = DoubleArray(6)
        val serr = StringBuilder()

        // Calculate Sun position
        val resSun = ephemeris.swe_calc_ut(jd, SwissEphemerisConstants.SE_SUN, 0, xx, serr)
        assertEquals(SwissEphemerisConstants.OK, resSun)
        assertTrue("Sun longitude out of range: ${xx[0]}", xx[0] in 0.0..360.0)
        // In early September, Sun is in Virgo (~160° longitude)
        val (sunSign, _) = ZodiacSign.fromLongitude(xx[0])
        assertEquals(ZodiacSign.VIRGO, sunSign)
    }

    @Test
    fun testNatalChartGeneration() {
        val profile = UserBirthProfile(
            fullName = "Carl Jung",
            year = 1875,
            month = 7,
            day = 26,
            hour = 19,
            minute = 29,
            isAm = false,
            cityName = "Kesswil, Switzerland",
            latitude = 47.5997,
            longitude = 9.3175,
            timeZoneId = "Europe/Zurich"
        )

        val chart = calculationEngine.calculateNatalChart(profile)
        assertNotNull(chart)
        assertEquals(ChartType.NATAL, chart.chartType)
        assertEquals(12, chart.houses.size)
        assertTrue(chart.planets.isNotEmpty())

        // Sun in Leo for July 26
        val sun = chart.planets.firstOrNull { it.planet == Planet.SUN }
        assertNotNull(sun)
        assertEquals(ZodiacSign.LEO, sun?.sign)
    }

    @Test
    fun testProgressedChartGeneration() {
        val profile = UserBirthProfile(
            fullName = "Seeker",
            year = 1990,
            month = 1,
            day = 1,
            hour = 12,
            minute = 0,
            isAm = false,
            cityName = "London, UK",
            latitude = 51.5074,
            longitude = -0.1278,
            timeZoneId = "Europe/London"
        )

        val progressed = calculationEngine.calculateProgressedChart(profile, 2026)
        assertEquals(ChartType.PROGRESSED, progressed.chartType)
        assertEquals(12, progressed.houses.size)
    }

    @Test
    fun testHoraryAndElectionalCalculations() {
        val (hChart, horary) = calculationEngine.calculateHorary(
            "Will the agreement finalize?", 40.7128, -74.0060, "New York, USA"
        )
        assertNotNull(hChart)
        assertNotNull(horary)
        assertTrue(horary.applyingAspect.isNotBlank())
        assertTrue(horary.horaryJudgment.isNotBlank())

        val (eChart, electional) = calculationEngine.calculateElectional(
            "Company Incorporation", 2026, 10, 1, 10, 0, 37.7749, -122.4194, "San Francisco, USA"
        )
        assertNotNull(eChart)
        assertNotNull(electional)
        assertTrue(electional.auspiciousScore in 1..100)
    }

    @Test
    fun testAstrocartographyAndMedicalAstrology() {
        val profile = UserBirthProfile()
        val lines = calculationEngine.calculateAstrocartography(profile)
        assertTrue(lines.isNotEmpty())
        assertTrue(lines.any { it.angle.contains("MC") })
        assertTrue(lines.any { it.angle.contains("AC") })

        val medical = calculationEngine.calculateMedicalAstrology(profile)
        assertNotNull(medical.dominantHumor)
        assertTrue(medical.elementDistribution.isNotEmpty())
        assertTrue(medical.sensitiveBodyParts.isNotEmpty())
    }

    @Test
    fun testTransitCacheExpirationRule() {
        val now = System.currentTimeMillis()

        // Fresh cache entry (10 minutes old)
        val freshTransit = TransitCacheEntity(
            cacheKey = "test_fresh",
            title = "Fresh Transit",
            julianDay = 2451545.0,
            dateTimeDisplay = "2026-09-04",
            locationName = "San Francisco",
            latitude = 37.7749,
            longitude = -122.4194,
            ascendant = 120.0,
            midheaven = 30.0,
            housesJson = "[]",
            planetsJson = "[]",
            aspectsJson = "[]",
            summaryReading = "Summary",
            cachedAtMillis = now - (10 * 60 * 1000L) // 10 mins ago
        )
        assertFalse("Transit cached 10 mins ago should not be expired", freshTransit.isExpired(now))

        // Expired cache entry (65 minutes old)
        val expiredTransit = freshTransit.copy(
            cacheKey = "test_expired",
            cachedAtMillis = now - (65 * 60 * 1000L) // 65 mins ago
        )
        assertTrue("Transit cached 65 mins ago must be expired (>1 hour rule)", expiredTransit.isExpired(now))
    }

    @Test
    fun testChartSerializationRoundTrip() {
        val profile = UserBirthProfile()
        val chart = calculationEngine.calculateNatalChart(profile)

        val entity = ChartSerializationHelper.chartToEntity(chart)
        val restored = ChartSerializationHelper.entityToChart(entity)

        assertEquals(chart.id, restored.id)
        assertEquals(chart.title, restored.title)
        assertEquals(chart.houses.size, restored.houses.size)
        assertEquals(chart.planets.size, restored.planets.size)
        assertEquals(chart.ascendant, restored.ascendant, 0.001)
        assertEquals(chart.midheaven, restored.midheaven, 0.001)
    }
}
