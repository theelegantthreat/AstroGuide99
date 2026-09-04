package com.example.astro.model

import java.util.Locale

/**
 * Representation of the 12 Zodiac Signs.
 */
enum class ZodiacSign(
    val signName: String,
    val symbol: String,
    val element: String,
    val quality: String,
    val ruler: String
) {
    ARIES("Aries", "♈", "Fire", "Cardinal", "Mars"),
    TAURUS("Taurus", "♉", "Earth", "Fixed", "Venus"),
    GEMINI("Gemini", "♊", "Air", "Mutable", "Mercury"),
    CANCER("Cancer", "♋", "Water", "Cardinal", "Moon"),
    LEO("Leo", "♌", "Fire", "Fixed", "Sun"),
    VIRGO("Virgo", "♍", "Earth", "Mutable", "Mercury"),
    LIBRA("Libra", "♎", "Air", "Cardinal", "Venus"),
    SCORPIO("Scorpio", "♏", "Water", "Fixed", "Pluto/Mars"),
    SAGITTARIUS("Sagittarius", "♐", "Fire", "Mutable", "Jupiter"),
    CAPRICORN("Capricorn", "♑", "Earth", "Cardinal", "Saturn"),
    AQUARIUS("Aquarius", "♒", "Air", "Fixed", "Uranus/Saturn"),
    PISCES("Pisces", "♓", "Water", "Mutable", "Neptune/Jupiter");

    companion object {
        fun fromLongitude(longitude: Double): Pair<ZodiacSign, Double> {
            val normalized = (longitude % 360.0 + 360.0) % 360.0
            val index = (normalized / 30.0).toInt().coerceIn(0, 11)
            val degreeInSign = normalized - (index * 30.0)
            return Pair(entries[index], degreeInSign)
        }
    }
}

/**
 * Astrological Celestial Bodies and Points with Swiss Ephemeris IDs.
 */
enum class Planet(
    val planetName: String,
    val glyph: String,
    val swissEphId: Int,
    val anatomicalRulership: String = ""
) {
    SUN("Sun", "☉", 0, "Heart, Circulation, Vitality, Spine"),
    MOON("Moon", "☽", 1, "Stomach, Digestive Fluids, Breasts, Bodily Fluids"),
    MERCURY("Mercury", "☿", 2, "Nervous System, Respiratory, Hands, Speech"),
    VENUS("Venus", "♀", 3, "Kidneys, Venous Circulation, Throat, Skin"),
    MARS("Mars", "♂", 4, "Muscular System, Adrenal Glands, Head, Blood/Iron"),
    JUPITER("Jupiter", "♃", 5, "Liver, Arterial System, Thighs, Cellular Growth"),
    SATURN("Saturn", "♄", 6, "Skeletal System, Bones, Teeth, Skin, Knees"),
    URANUS("Uranus", "♅", 7, "Nervous impulses, Spasms, Electrical synapse"),
    NEPTUNE("Neptune", "♆", 8, "Lymphatic System, Immune response, Feet, Pineal"),
    PLUTO("Pluto", "♇", 9, "Reproductive organs, Elimination, Deep cellular regeneration"),
    NORTH_NODE("North Node", "☊", 10, "Karmic assimilation, Head of Dragon"),
    SOUTH_NODE("South Node", "☋", 11, "Karmic release, Tail of Dragon"),
    CHIRON("Chiron", "⚷", 12, "Deep healing, Energetic sensitivity");

    companion object {
        fun fromId(id: Int): Planet? = entries.find { it.swissEphId == id }
    }
}

/**
 * Planetary placement in ecliptic coordinates.
 */
data class PlanetaryPlacement(
    val planet: Planet,
    val longitude: Double,
    val latitude: Double = 0.0,
    val speed: Double = 0.0,
    val sign: ZodiacSign,
    val degreeInSign: Double,
    val house: Int,
    val isRetrograde: Boolean = false
) {
    val formattedPlacement: String
        get() {
            val deg = degreeInSign.toInt()
            val min = ((degreeInSign - deg) * 60).toInt()
            val retro = if (isRetrograde) " ℞" else ""
            return String.format(Locale.US, "%02d° %s %02d'%s", deg, sign.symbol, min, retro)
        }
}

/**
 * Placidus House Cusp.
 */
data class HouseCusp(
    val number: Int,
    val longitude: Double,
    val sign: ZodiacSign,
    val degreeInSign: Double
) {
    val formatted: String
        get() {
            val deg = degreeInSign.toInt()
            val min = ((degreeInSign - deg) * 60).toInt()
            return String.format(Locale.US, "H%d: %02d° %s %02d'", number, deg, sign.symbol, min)
        }
}

/**
 * Astrological Aspect between two planets.
 */
enum class AspectType(val aspectName: String, val glyph: String, val angle: Double, val orbAllowed: Double) {
    CONJUNCTION("Conjunction", "☌", 0.0, 8.0),
    SEXTILE("Sextile", "⚹", 60.0, 6.0),
    SQUARE("Square", "□", 90.0, 7.0),
    TRINE("Trine", "△", 120.0, 8.0),
    OPPOSITION("Opposition", "☍", 180.0, 8.0)
}

data class Aspect(
    val planet1: Planet,
    val planet2: Planet,
    val type: AspectType,
    val exactAngle: Double,
    val orb: Double
)

/**
 * Master Chart Data Model.
 */
data class ChartData(
    val id: String,
    val title: String,
    val chartType: ChartType,
    val julianDay: Double,
    val dateTimeDisplay: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val ascendant: Double,
    val midheaven: Double,
    val houses: List<HouseCusp>,
    val planets: List<PlanetaryPlacement>,
    val aspects: List<Aspect> = emptyList(),
    val summaryReading: String = ""
)

enum class ChartType(val title: String, val subtitle: String) {
    NATAL("Natal Chart", "Radix Birth Horoscope"),
    PROGRESSED("Progressed Chart", "Secondary Day-for-Year Progressions"),
    TRANSITS("Transits", "Current Planetary Transits"),
    HORARY("Horary Astrology", "Chart of the Question Moment"),
    ELECTIONAL("Electional Astrology", "Auspicious Timing Selection"),
    LOCATIONAL("Locational (Astrocartography)", "Planetary Angular Power Lines"),
    MEDICAL("Medical Astrology", "Humoral Temperaments & Decumbiture")
}

/**
 * User birth data model stored in DataStore and entered via Compose UI.
 */
data class UserBirthProfile(
    val fullName: String = "Seeker",
    val year: Int = 1990,
    val month: Int = 6,
    val day: Int = 15,
    val hour: Int = 10,
    val minute: Int = 30,
    val isAm: Boolean = true,
    val cityName: String = "San Francisco, USA",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val timeZoneId: String = "America/Los_Angeles"
) {
    val birthDateDisplay: String
        get() = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    val birthTimeDisplay: String
        get() {
            val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val period = if (isAm) "AM" else "PM"
            return String.format(Locale.US, "%02d:%02d %s", h12, minute, period)
        }

    val hour24: Int
        get() {
            return if (isAm) {
                if (hour == 12) 0 else hour
            } else {
                if (hour == 12) 12 else hour + 12
            }
        }
}

/**
 * Models for specialized calculation branches.
 */
data class HoraryData(
    val question: String,
    val planetaryHourRuler: Planet,
    val querentSignificator: Planet,
    val quesitedSignificator: Planet,
    val applyingAspect: String,
    val dignityScore: String,
    val horaryJudgment: String
)

data class ElectionalData(
    val purpose: String,
    val targetDateDisplay: String,
    val moonPhase: String,
    val isMoonVoidOfCourse: Boolean,
    val planetaryHour: String,
    val auspiciousScore: Int, // 1 to 100
    val favorableFactors: List<String>,
    val afflictions: List<String>,
    val recommendation: String
)

data class AstrocartographyLine(
    val planet: Planet,
    val angle: String, // AC, MC, DC, IC
    val primeLongitude: Double,
    val citiesInfluenced: List<String>,
    val psychologicalTheme: String
)

data class MedicalAstrologyData(
    val dominantHumor: String, // Choleric, Sanguine, Melancholic, Phlegmatic
    val elementDistribution: Map<String, Int>,
    val sensitiveBodyParts: List<String>,
    val planetaryAilments: List<Pair<Planet, String>>,
    val holisticRecommendation: String
)
