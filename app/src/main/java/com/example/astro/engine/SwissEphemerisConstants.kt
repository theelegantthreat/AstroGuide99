package com.example.astro.engine

/**
 * Standard Swiss Ephemeris Constants.
 * Matches SE_* definitions from the official Swiss Ephemeris C / Java specifications.
 */
object SwissEphemerisConstants {
    // Planet / Body Identifiers
    const val SE_SUN = 0
    const val SE_MOON = 1
    const val SE_MERCURY = 2
    const val SE_VENUS = 3
    const val SE_MARS = 4
    const val SE_JUPITER = 5
    const val SE_SATURN = 6
    const val SE_URANUS = 7
    const val SE_NEPTUNE = 8
    const val SE_PLUTO = 9
    const val SE_MEAN_NODE = 10
    const val SE_TRUE_NODE = 11
    const val SE_CHIRON = 15

    // Calculation Flags
    const val SEFLG_JPLEPH = 1
    const val SEFLG_SWIEPH = 2
    const val SEFLG_MOSEPH = 4
    const val SEFLG_HELIO = 8
    const val SEFLG_TRUEPOS = 16
    const val SEFLG_J2000 = 32
    const val SEFLG_NONUT = 64
    const val SEFLG_SPEED3 = 128
    const val SEFLG_SPEED = 256
    const val SEFLG_NOGDEFL = 512
    const val SEFLG_NOABERR = 1024
    const val SEFLG_EQUATORIAL = 2048
    const val SEFLG_XYZ = 4096
    const val SEFLG_RADIANS = 8192
    const val SEFLG_BARYCTR = 16384
    const val SEFLG_TOPOCTR = (32 * 1024)
    const val SEFLG_SIDEREAL = (64 * 1024)

    // House Systems
    const val SE_HSYS_PLACIDUS = 'P'
    const val SE_HSYS_KOCH = 'K'
    const val SE_HSYS_PORPHYRIUS = 'O'
    const val SE_HSYS_REGIOMONTANUS = 'R'
    const val SE_HSYS_CAMPANUS = 'C'
    const val SE_HSYS_EQUAL = 'E'
    const val SE_HSYS_WHOLE_SIGN = 'W'

    // Error codes
    const val OK = 0
    const val ERR = -1
}
