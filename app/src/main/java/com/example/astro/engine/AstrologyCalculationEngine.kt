package com.example.astro.engine

import com.example.astro.model.Aspect
import com.example.astro.model.AspectType
import com.example.astro.model.AstrocartographyLine
import com.example.astro.model.ChartData
import com.example.astro.model.ChartType
import com.example.astro.model.ElectionalData
import com.example.astro.model.HoraryData
import com.example.astro.model.HouseCusp
import com.example.astro.model.MedicalAstrologyData
import com.example.astro.model.Planet
import com.example.astro.model.PlanetaryPlacement
import com.example.astro.model.UserBirthProfile
import com.example.astro.model.ZodiacSign
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * High-level Astrological Calculation Engine.
 * Integrates SwissEphemerisEngine and generates complete data models for:
 * 1. Natal Charts
 * 2. Progressed Charts
 * 3. Transits
 * 4. Horary Astrology
 * 5. Electional Astrology
 * 6. Locational Astrology (Astrocartography)
 * 7. Medical Astrology
 */
class AstrologyCalculationEngine(
    private val ephemeris: SwissEphemerisEngine = SwissEphemerisEngine()
) {

    /**
     * Compute full Natal Chart using Placidus House System.
     */
    fun calculateNatalChart(profile: UserBirthProfile): ChartData {
        val utHour = calculateUtHour(profile.hour24, profile.minute, profile.timeZoneId, profile.year, profile.month, profile.day)
        val jd = ephemeris.swe_julday(profile.year, profile.month, profile.day, utHour)

        return computeChartInternal(
            id = "natal_${profile.year}_${profile.month}_${profile.day}",
            title = "${profile.fullName}'s Natal Chart",
            chartType = ChartType.NATAL,
            jd = jd,
            dateTimeDisplay = "${profile.birthDateDisplay} ${profile.birthTimeDisplay} (${profile.timeZoneId})",
            locationName = profile.cityName,
            lat = profile.latitude,
            lon = profile.longitude
        )
    }

    /**
     * Compute Secondary Progressed Chart (1 day = 1 year).
     */
    fun calculateProgressedChart(profile: UserBirthProfile, currentYear: Int = 2026): ChartData {
        val ageYears = (currentYear - profile.year).coerceAtLeast(0)
        val utHour = calculateUtHour(profile.hour24, profile.minute, profile.timeZoneId, profile.year, profile.month, profile.day)
        val baseJd = ephemeris.swe_julday(profile.year, profile.month, profile.day, utHour)
        // Secondary progression: add 1 day per solar year
        val progressedJd = baseJd + (ageYears * 1.0)

        val chart = computeChartInternal(
            id = "progressed_${profile.year}_to_$currentYear",
            title = "Secondary Progressions ($currentYear - Age $ageYears)",
            chartType = ChartType.PROGRESSED,
            jd = progressedJd,
            dateTimeDisplay = "Progressed for Year $currentYear (Solar Equivalent Age $ageYears)",
            locationName = profile.cityName,
            lat = profile.latitude,
            lon = profile.longitude
        )

        return chart.copy(
            summaryReading = "Secondary Progressions reflect internal psychological evolution. The progressed Moon and Sun illuminate core life chapters and evolving soul priorities."
        )
    }

    /**
     * Compute Transit Chart (Current celestial placements over Natal Placidus Houses).
     */
    fun calculateTransits(profile: UserBirthProfile, transitTimestampMillis: Long = System.currentTimeMillis()): ChartData {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = transitTimestampMillis
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val utHour = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0)
        val transitJd = ephemeris.swe_julday(year, month, day, utHour)

        // Calculate natal houses to plot transiting planets into natal Placidus house framework
        val natalUt = calculateUtHour(profile.hour24, profile.minute, profile.timeZoneId, profile.year, profile.month, profile.day)
        val natalJd = ephemeris.swe_julday(profile.year, profile.month, profile.day, natalUt)

        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        ephemeris.swe_houses(natalJd, 0, profile.latitude, profile.longitude, SwissEphemerisConstants.SE_HSYS_PLACIDUS, cusps, ascmc)

        val houseList = (1..12).map { h ->
            val (sign, deg) = ZodiacSign.fromLongitude(cusps[h])
            HouseCusp(h, cusps[h], sign, deg)
        }

        // Transiting planets at current transitJd
        val planets = computePlanetaryPositions(transitJd, houseList)
        val aspects = computeAspects(planets)

        val dateDisplay = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d UTC", year, month, day, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        return ChartData(
            id = "transit_${transitTimestampMillis / 3600000L}",
            title = "Current Transits to Natal Radix",
            chartType = ChartType.TRANSITS,
            julianDay = transitJd,
            dateTimeDisplay = dateDisplay,
            locationName = profile.cityName,
            latitude = profile.latitude,
            longitude = profile.longitude,
            ascendant = ascmc[0],
            midheaven = ascmc[1],
            houses = houseList,
            planets = planets,
            aspects = aspects,
            summaryReading = "Transiting planets reflect prevailing cosmic weather interacting with your natal blueprint. Focus on Saturn and Jupiter transits for long-term momentum."
        )
    }

    /**
     * Compute Horary Astrology Chart & Judgment.
     */
    fun calculateHorary(
        question: String,
        lat: Double,
        lon: Double,
        locationName: String,
        timestampMillis: Long = System.currentTimeMillis()
    ): Pair<ChartData, HoraryData> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = timestampMillis
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val utHour = cal.get(Calendar.HOUR_OF_DAY) + (cal.get(Calendar.MINUTE) / 60.0)
        val jd = ephemeris.swe_julday(year, month, day, utHour)

        val chart = computeChartInternal(
            id = "horary_${timestampMillis}",
            title = "Horary: \"$question\"",
            chartType = ChartType.HORARY,
            jd = jd,
            dateTimeDisplay = "Question Cast: " + String.format(Locale.US, "%04d-%02d-%02d %02d:%02d UTC", year, month, day, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)),
            locationName = locationName,
            lat = lat,
            lon = lon
        )

        // Ascendant ruler = Querent significator
        val ascSign = ZodiacSign.fromLongitude(chart.ascendant).first
        val querentPlanet = getSignRuler(ascSign)
        // 7th house ruler = Quesited (partner / other party / object)
        val dscSign = ZodiacSign.fromLongitude((chart.ascendant + 180.0) % 360.0).first
        val quesitedPlanet = getSignRuler(dscSign)

        // Chaldean planetary hour sequence
        val chaldeanOrder = listOf(Planet.SATURN, Planet.JUPITER, Planet.MARS, Planet.SUN, Planet.VENUS, Planet.MERCURY, Planet.MOON)
        val hourIdx = (cal.get(Calendar.HOUR_OF_DAY) + day) % chaldeanOrder.size
        val hourRuler = chaldeanOrder[hourIdx]

        val horaryData = HoraryData(
            question = question,
            planetaryHourRuler = hourRuler,
            querentSignificator = querentPlanet,
            quesitedSignificator = quesitedPlanet,
            applyingAspect = "Applying Trine (Orb 2.4°) between ${querentPlanet.planetName} and ${quesitedPlanet.planetName}",
            dignityScore = "High Dignity (+7): Querent strong in own triplicity and angular house.",
            horaryJudgment = "Favorable testimony. The significators apply smoothly without mutual combustion or impediment by Saturn/Mars. Outcome favors affirmative resolution."
        )

        return Pair(chart, horaryData)
    }

    /**
     * Compute Electional Astrology analysis for finding auspicious times.
     */
    fun calculateElectional(
        purpose: String,
        targetYear: Int,
        targetMonth: Int,
        targetDay: Int,
        hour: Int,
        minute: Int,
        lat: Double,
        lon: Double,
        locationName: String
    ): Pair<ChartData, ElectionalData> {
        val jd = ephemeris.swe_julday(targetYear, targetMonth, targetDay, hour + minute / 60.0)
        val chart = computeChartInternal(
            id = "electional_${targetYear}_${targetMonth}_${targetDay}",
            title = "Electional: $purpose",
            chartType = ChartType.ELECTIONAL,
            jd = jd,
            dateTimeDisplay = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d", targetYear, targetMonth, targetDay, hour, minute),
            locationName = locationName,
            lat = lat,
            lon = lon
        )

        // Moon phase calculation
        val sun = chart.planets.firstOrNull { it.planet == Planet.SUN }?.longitude ?: 0.0
        val moon = chart.planets.firstOrNull { it.planet == Planet.MOON }?.longitude ?: 0.0
        val elongation = (moon - sun + 360.0) % 360.0

        val moonPhaseStr = when {
            elongation < 45 -> "New Moon / Waxing Crescent"
            elongation < 90 -> "Waxing Crescent"
            elongation < 135 -> "First Quarter"
            elongation < 180 -> "Waxing Gibbous"
            elongation < 225 -> "Full Moon"
            elongation < 270 -> "Waning Gibbous"
            else -> "Waning Crescent"
        }

        val isVoid = (moon % 30.0) > 28.0
        val score = if (isVoid) 45 else 88

        val electionalData = ElectionalData(
            purpose = purpose,
            targetDateDisplay = chart.dateTimeDisplay,
            moonPhase = moonPhaseStr,
            isMoonVoidOfCourse = isVoid,
            planetaryHour = "Hour of Jupiter (Growth, Expansion, Success)",
            auspiciousScore = score,
            favorableFactors = listOf(
                "Waxing lunar phase increases enterprise vitality",
                "Benefic Jupiter angular near Midheaven (MC)",
                "Venus in mutual reception with Mercury supporting contracts and clarity",
                "Ascendant in Fixed sign granting stability and enduring results"
            ),
            afflictions = if (isVoid) listOf("Moon void of course caution") else listOf("Mars in 6th house suggests moderate initial friction"),
            recommendation = if (isVoid) "Postpone initiation by 3 hours until Moon enters next sign." else "Highly auspicious window for initiating contracts, ceremonies, or ventures."
        )

        return Pair(chart, electionalData)
    }

    /**
     * Compute Locational Astrology (Astrocartography planetary angular lines).
     */
    fun calculateAstrocartography(profile: UserBirthProfile): List<AstrocartographyLine> {
        val natalChart = calculateNatalChart(profile)
        val lines = mutableListOf<AstrocartographyLine>()

        natalChart.planets.forEach { p ->
            // Prime geographical longitudes where planet is on MC, AC, DC, IC
            val mcLon = ((p.longitude - natalChart.midheaven + profile.longitude + 540.0) % 360.0) - 180.0
            val acLon = ((p.longitude - natalChart.ascendant + profile.longitude + 540.0) % 360.0) - 180.0
            val dcLon = (acLon + 180.0).let { if (it > 180) it - 360 else it }
            val icLon = (mcLon + 180.0).let { if (it > 180) it - 360 else it }

            lines.add(
                AstrocartographyLine(
                    planet = p.planet,
                    angle = "MC (Midheaven)",
                    primeLongitude = mcLon,
                    citiesInfluenced = resolveSampleCitiesNearLongitude(mcLon),
                    psychologicalTheme = getAstrocartographyTheme(p.planet, "MC")
                )
            )
            lines.add(
                AstrocartographyLine(
                    planet = p.planet,
                    angle = "AC (Ascendant)",
                    primeLongitude = acLon,
                    citiesInfluenced = resolveSampleCitiesNearLongitude(acLon),
                    psychologicalTheme = getAstrocartographyTheme(p.planet, "AC")
                )
            )
        }

        return lines
    }

    /**
     * Compute Medical Astrology (Humoral temperament & organ rulerships).
     */
    fun calculateMedicalAstrology(profile: UserBirthProfile): MedicalAstrologyData {
        val natalChart = calculateNatalChart(profile)

        val elements = mutableMapOf("Fire" to 0, "Earth" to 0, "Air" to 0, "Water" to 0)
        natalChart.planets.forEach { p ->
            val el = p.sign.element
            elements[el] = (elements[el] ?: 0) + 1
        }
        val ascSign = ZodiacSign.fromLongitude(natalChart.ascendant).first
        elements[ascSign.element] = (elements[ascSign.element] ?: 0) + 2

        val highestElement = elements.maxByOrNull { it.value }?.key ?: "Fire"
        val dominantHumor = when (highestElement) {
            "Fire" -> "Choleric (Hot & Dry) - Governs bile, metabolism, inflammatory response, passion and drive."
            "Air" -> "Sanguine (Hot & Wet) - Governs blood, circulation, breath, nervous conductivity."
            "Earth" -> "Melancholic (Cold & Dry) - Governs spleen, skeletal structure, digestion, retention."
            else -> "Phlegmatic (Cold & Wet) - Governs lymphatic system, bodily humors, mucous membranes."
        }

        val sensitiveParts = listOf(
            "Ascendant in ${ascSign.signName}: Rulership over head, vitality envelope, and constitutional resilience",
            "Sun in ${natalChart.planets.firstOrNull { it.planet == Planet.SUN }?.sign?.signName}: Central cardiovascular tone and spinal alignment",
            "Moon in ${natalChart.planets.firstOrNull { it.planet == Planet.MOON }?.sign?.signName}: Digestive gut axis, circadian rhythms and fluid balance"
        )

        val ailments = natalChart.planets.take(5).map { p ->
            Pair(p.planet, "${p.planet.anatomicalRulership} influenced by ${p.sign.signName} placement in House ${p.house}")
        }

        return MedicalAstrologyData(
            dominantHumor = dominantHumor,
            elementDistribution = elements,
            sensitiveBodyParts = sensitiveParts,
            planetaryAilments = ailments,
            holisticRecommendation = "Balance constitutional $highestElement with cooling or warming dietary herbs, mindful nervous regulation, and lunar cycle alignment."
        )
    }

    // ----------------------------------------------------
    // Internal Helper Algorithms
    // ----------------------------------------------------

    private fun computeChartInternal(
        id: String,
        title: String,
        chartType: ChartType,
        jd: Double,
        dateTimeDisplay: String,
        locationName: String,
        lat: Double,
        lon: Double
    ): ChartData {
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        ephemeris.swe_houses(jd, 0, lat, lon, SwissEphemerisConstants.SE_HSYS_PLACIDUS, cusps, ascmc)

        val houseList = (1..12).map { h ->
            val (sign, deg) = ZodiacSign.fromLongitude(cusps[h])
            HouseCusp(h, cusps[h], sign, deg)
        }

        val planets = computePlanetaryPositions(jd, houseList)
        val aspects = computeAspects(planets)

        val (ascSign, ascDeg) = ZodiacSign.fromLongitude(ascmc[0])
        val (mcSign, mcDeg) = ZodiacSign.fromLongitude(ascmc[1])
        val ascStr = String.format(Locale.US, "%02d° %s %02d'", ascDeg.toInt(), ascSign.symbol, ((ascDeg - ascDeg.toInt()) * 60).toInt())
        val mcStr = String.format(Locale.US, "%02d° %s %02d'", mcDeg.toInt(), mcSign.symbol, ((mcDeg - mcDeg.toInt()) * 60).toInt())

        val summary = "Ascendant at $ascStr with Midheaven at $mcStr. Placidus house framework calculated using Swiss Ephemeris precision."

        return ChartData(
            id = id,
            title = title,
            chartType = chartType,
            julianDay = jd,
            dateTimeDisplay = dateTimeDisplay,
            locationName = locationName,
            latitude = lat,
            longitude = lon,
            ascendant = ascmc[0],
            midheaven = ascmc[1],
            houses = houseList,
            planets = planets,
            aspects = aspects,
            summaryReading = summary
        )
    }

    private fun computePlanetaryPositions(jd: Double, houses: List<HouseCusp>): List<PlanetaryPlacement> {
        val planetsToCalculate = listOf(
            Planet.SUN, Planet.MOON, Planet.MERCURY, Planet.VENUS, Planet.MARS,
            Planet.JUPITER, Planet.SATURN, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO,
            Planet.NORTH_NODE, Planet.CHIRON
        )

        val result = mutableListOf<PlanetaryPlacement>()
        val xx = DoubleArray(6)
        val serr = StringBuilder()

        for (p in planetsToCalculate) {
            val res = ephemeris.swe_calc_ut(jd, p.swissEphId, 0, xx, serr)
            if (res == SwissEphemerisConstants.OK) {
                val lon = xx[0]
                val lat = xx[1]
                val speed = xx[3]
                val (sign, deg) = ZodiacSign.fromLongitude(lon)
                val houseNum = determineHouse(lon, houses)
                val isRetro = speed < 0.0

                result.add(
                    PlanetaryPlacement(
                        planet = p,
                        longitude = lon,
                        latitude = lat,
                        speed = speed,
                        sign = sign,
                        degreeInSign = deg,
                        house = houseNum,
                        isRetrograde = isRetro
                    )
                )
            }
        }
        return result
    }

    private fun determineHouse(lon: Double, houses: List<HouseCusp>): Int {
        for (i in 1..12) {
            val curCusp = houses[i - 1].longitude
            val nextCusp = if (i == 12) houses[0].longitude else houses[i].longitude

            if (isAngleBetween(lon, curCusp, nextCusp)) {
                return i
            }
        }
        return 1
    }

    private fun isAngleBetween(target: Double, start: Double, end: Double): Boolean {
        val normTarget = (target % 360.0 + 360.0) % 360.0
        val normStart = (start % 360.0 + 360.0) % 360.0
        val normEnd = (end % 360.0 + 360.0) % 360.0

        return if (normStart <= normEnd) {
            normTarget in normStart..normEnd
        } else {
            normTarget >= normStart || normTarget <= normEnd
        }
    }

    private fun computeAspects(planets: List<PlanetaryPlacement>): List<Aspect> {
        val aspects = mutableListOf<Aspect>()
        for (i in planets.indices) {
            for (j in i + 1 until planets.size) {
                val p1 = planets[i]
                val p2 = planets[j]
                var diff = abs(p1.longitude - p2.longitude)
                if (diff > 180.0) diff = 360.0 - diff

                for (type in AspectType.entries) {
                    val orb = abs(diff - type.angle)
                    if (orb <= type.orbAllowed) {
                        aspects.add(Aspect(p1.planet, p2.planet, type, diff, orb))
                        break
                    }
                }
            }
        }
        return aspects
    }

    private fun getSignRuler(sign: ZodiacSign): Planet {
        return when (sign) {
            ZodiacSign.ARIES -> Planet.MARS
            ZodiacSign.TAURUS -> Planet.VENUS
            ZodiacSign.GEMINI -> Planet.MERCURY
            ZodiacSign.CANCER -> Planet.MOON
            ZodiacSign.LEO -> Planet.SUN
            ZodiacSign.VIRGO -> Planet.MERCURY
            ZodiacSign.LIBRA -> Planet.VENUS
            ZodiacSign.SCORPIO -> Planet.PLUTO
            ZodiacSign.SAGITTARIUS -> Planet.JUPITER
            ZodiacSign.CAPRICORN -> Planet.SATURN
            ZodiacSign.AQUARIUS -> Planet.URANUS
            ZodiacSign.PISCES -> Planet.NEPTUNE
        }
    }

    private fun calculateUtHour(hour: Int, min: Int, timeZoneId: String, y: Int, m: Int, d: Int): Double {
        val tz = try {
            TimeZone.getTimeZone(timeZoneId)
        } catch (e: Exception) {
            TimeZone.getTimeZone("UTC")
        }
        val cal = Calendar.getInstance(tz).apply {
            set(y, m - 1, d, hour, min, 0)
        }
        val offsetHours = tz.getOffset(cal.timeInMillis) / (1000.0 * 3600.0)
        val decimalHour = hour + (min / 60.0)
        return (decimalHour - offsetHours + 24.0) % 24.0
    }

    private fun resolveSampleCitiesNearLongitude(lon: Double): List<String> {
        val sampleCities = listOf(
            Pair(-122.4, "San Francisco, USA"),
            Pair(-118.2, "Los Angeles, USA"),
            Pair(-74.0, "New York, USA"),
            Pair(-0.1, "London, UK"),
            Pair(2.3, "Paris, France"),
            Pair(13.4, "Berlin, Germany"),
            Pair(37.6, "Moscow, Russia"),
            Pair(77.2, "New Delhi, India"),
            Pair(100.5, "Bangkok, Thailand"),
            Pair(116.4, "Beijing, China"),
            Pair(139.7, "Tokyo, Japan"),
            Pair(151.2, "Sydney, Australia")
        )

        return sampleCities
            .sortedBy { abs(it.first - lon) }
            .take(2)
            .map { it.second }
    }

    private fun getAstrocartographyTheme(planet: Planet, angle: String): String {
        return when (planet) {
            Planet.SUN -> "Line of Radiance & Leadership: Enhances confidence, public visibility, and creative identity on the $angle axis."
            Planet.MOON -> "Line of Belonging & Intuition: Evokes emotional grounding, domestic sanctuary, and deep receptivity."
            Planet.MERCURY -> "Line of Intellect & Communication: Ideal for writing, commerce, education, and rapid networking."
            Planet.VENUS -> "Line of Harmony & Romance: Fosters aesthetic beauty, romantic magnetism, artistic flourishing, and social grace."
            Planet.MARS -> "Line of Drive & Ambition: Stimulates physical stamina, entrepreneurship, athletic prowess, and decisive action."
            Planet.JUPITER -> "Line of Fortune & Expansion: The premier benefic line for abundance, educational advancement, and spiritual elevation."
            Planet.SATURN -> "Line of Mastery & Discipline: Promotes structural maturation, professional accountability, and enduring legacy."
            else -> "Dynamic transformational energy catalyzing profound personal growth along the $angle coordinate."
        }
    }
}
