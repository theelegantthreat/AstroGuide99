package com.example.astro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.astro.model.UserBirthProfile
import java.util.Calendar
import java.util.TimeZone

/**
 * City Preset Data for rapid resolution of Place -> Latitude, Longitude & IANA Time Zone.
 */
data class CityPreset(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

val POPULAR_CITIES = listOf(
    CityPreset("San Francisco, USA", 37.7749, -122.4194, "America/Los_Angeles"),
    CityPreset("New York, USA", 40.7128, -74.0060, "America/New_York"),
    CityPreset("London, UK", 51.5074, -0.1278, "Europe/London"),
    CityPreset("Paris, France", 48.8566, 2.3522, "Europe/Paris"),
    CityPreset("Tokyo, Japan", 35.6762, 139.6503, "Asia/Tokyo"),
    CityPreset("Sydney, Australia", -33.8688, 151.2093, "Australia/Sydney"),
    CityPreset("New Delhi, India", 28.6139, 77.2090, "Asia/Kolkata"),
    CityPreset("Cairo, Egypt", 30.0444, 31.2357, "Africa/Cairo")
)

/**
 * Declarative Material 3 user input screen to collect:
 * - Date of birth (DatePicker)
 * - Time of birth (12-hour clock with AM/PM toggle)
 * - Place of birth (City resolving to Latitude/Longitude floats)
 * - Time Zone ID (IANA string)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BirthDataInputSection(
    profile: UserBirthProfile,
    onSaveProfile: (UserBirthProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember(profile) { mutableStateOf(profile.fullName) }
    var year by remember(profile) { mutableStateOf(profile.year) }
    var month by remember(profile) { mutableStateOf(profile.month) }
    var day by remember(profile) { mutableStateOf(profile.day) }
    var hour12 by remember(profile) { mutableStateOf(if (profile.hour == 0) 12 else if (profile.hour > 12) profile.hour - 12 else profile.hour) }
    var minute by remember(profile) { mutableStateOf(profile.minute) }
    var isAm by remember(profile) { mutableStateOf(profile.isAm) }
    var cityName by remember(profile) { mutableStateOf(profile.cityName) }
    var latitude by remember(profile) { mutableStateOf(profile.latitude.toString()) }
    var longitude by remember(profile) { mutableStateOf(profile.longitude.toString()) }
    var timeZoneId by remember(profile) { mutableStateOf(profile.timeZoneId) }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAdvancedCoordinates by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("birth_data_input_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Natal Birth Coordinates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Seeker Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_full_name")
            )

            // Date of Birth Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = String.format("%04d-%02d-%02d", year, month, day),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date of Birth") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.testTag("btn_pick_date")
                        ) {
                            Text("Select")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Time of Birth (12-Hour Clock with AM/PM toggle)
            Text(
                text = "Time of Birth (12-Hour Clock)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (hour12 == 0) "" else hour12.toString(),
                    onValueChange = {
                        val num = it.toIntOrNull()
                        if (num != null && num in 1..12) hour12 = num
                        else if (it.isEmpty()) hour12 = 0
                    },
                    label = { Text("Hour (1-12)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_hour")
                )

                OutlinedTextField(
                    value = String.format("%02d", minute),
                    onValueChange = {
                        val num = it.toIntOrNull()
                        if (num != null && num in 0..59) minute = num
                    },
                    label = { Text("Min (00-59)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_minute")
                )

                // AM / PM Toggle buttons
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = isAm,
                        onClick = { isAm = true },
                        label = { Text("AM") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("chip_am")
                    )
                    FilterChip(
                        selected = !isAm,
                        onClick = { isAm = false },
                        label = { Text("PM") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("chip_pm")
                    )
                }
            }

            // Place of Birth & Quick Presets
            Text(
                text = "Place of Birth (Resolves Lat/Lon & Time Zone)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = cityName,
                onValueChange = { cityName = it },
                label = { Text("City, Country") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_city_name")
            )

            // Popular City Presets for Quick Selection
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                POPULAR_CITIES.forEach { preset ->
                    FilterChip(
                        selected = cityName == preset.cityName,
                        onClick = {
                            cityName = preset.cityName
                            latitude = preset.latitude.toString()
                            longitude = preset.longitude.toString()
                            timeZoneId = preset.timeZoneId
                        },
                        label = { Text(preset.cityName.substringBefore(",")) }
                    )
                }
            }

            // Time Zone & Coordinates Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Zone: $timeZoneId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                TextButton(onClick = { showAdvancedCoordinates = !showAdvancedCoordinates }) {
                    Text(if (showAdvancedCoordinates) "Hide Lat/Lon" else "Edit Lat/Lon")
                }
            }

            if (showAdvancedCoordinates) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = timeZoneId,
                    onValueChange = { timeZoneId = it },
                    label = { Text("IANA Time Zone ID (e.g., America/New_York)") },
                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save and Recalculate Button
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()
                    val hr = if (hour12 == 0) 12 else hour12

                    if (lat == null || lat !in -90.0..90.0) {
                        errorMessage = "Invalid Latitude (-90 to +90)"
                        return@Button
                    }
                    if (lon == null || lon !in -180.0..180.0) {
                        errorMessage = "Invalid Longitude (-180 to +180)"
                        return@Button
                    }

                    errorMessage = null
                    val updated = profile.copy(
                        fullName = fullName.ifBlank { "Seeker" },
                        year = year,
                        month = month,
                        day = day,
                        hour = hr,
                        minute = minute,
                        isAm = isAm,
                        cityName = cityName.ifBlank { "Unknown City" },
                        latitude = lat,
                        longitude = lon,
                        timeZoneId = timeZoneId.ifBlank { "UTC" }
                    )
                    onSaveProfile(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_calculate"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile & Compute Ephemeris", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Calendar.getInstance().apply {
                set(year, month - 1, day)
            }.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            year = cal.get(Calendar.YEAR)
                            month = cal.get(Calendar.MONTH) + 1
                            day = cal.get(Calendar.DAY_OF_MONTH)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
