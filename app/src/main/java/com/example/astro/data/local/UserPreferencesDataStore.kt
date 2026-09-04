package com.example.astro.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.astro.model.UserBirthProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "astroguide_user_profile")

/**
 * DataStore manager for persistent User Birth Input details.
 */
class UserPreferencesDataStore(private val context: Context) {

    private val KEY_FULL_NAME = stringPreferencesKey("full_name")
    private val KEY_YEAR = intPreferencesKey("birth_year")
    private val KEY_MONTH = intPreferencesKey("birth_month")
    private val KEY_DAY = intPreferencesKey("birth_day")
    private val KEY_HOUR = intPreferencesKey("birth_hour")
    private val KEY_MINUTE = intPreferencesKey("birth_minute")
    private val KEY_IS_AM = booleanPreferencesKey("birth_is_am")
    private val KEY_CITY_NAME = stringPreferencesKey("city_name")
    private val KEY_LATITUDE = doublePreferencesKey("latitude")
    private val KEY_LONGITUDE = doublePreferencesKey("longitude")
    private val KEY_TIME_ZONE_ID = stringPreferencesKey("time_zone_id")

    val userProfileFlow: Flow<UserBirthProfile> = context.userPreferencesDataStore.data.map { prefs ->
        UserBirthProfile(
            fullName = prefs[KEY_FULL_NAME] ?: "Seeker",
            year = prefs[KEY_YEAR] ?: 1995,
            month = prefs[KEY_MONTH] ?: 5,
            day = prefs[KEY_DAY] ?: 21,
            hour = prefs[KEY_HOUR] ?: 10,
            minute = prefs[KEY_MINUTE] ?: 30,
            isAm = prefs[KEY_IS_AM] ?: true,
            cityName = prefs[KEY_CITY_NAME] ?: "San Francisco, USA",
            latitude = prefs[KEY_LATITUDE] ?: 37.7749,
            longitude = prefs[KEY_LONGITUDE] ?: -122.4194,
            timeZoneId = prefs[KEY_TIME_ZONE_ID] ?: "America/Los_Angeles"
        )
    }

    suspend fun saveUserProfile(profile: UserBirthProfile) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_YEAR] = profile.year
            prefs[KEY_MONTH] = profile.month
            prefs[KEY_DAY] = profile.day
            prefs[KEY_HOUR] = profile.hour
            prefs[KEY_MINUTE] = profile.minute
            prefs[KEY_IS_AM] = profile.isAm
            prefs[KEY_CITY_NAME] = profile.cityName
            prefs[KEY_LATITUDE] = profile.latitude
            prefs[KEY_LONGITUDE] = profile.longitude
            prefs[KEY_TIME_ZONE_ID] = profile.timeZoneId
        }
    }
}
