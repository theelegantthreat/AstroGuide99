package com.example.astro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.astro.model.AstrocartographyLine
import com.example.astro.model.ChartData
import com.example.astro.model.ElectionalData
import com.example.astro.model.HoraryData
import com.example.astro.model.MedicalAstrologyData
import com.example.astro.model.UserBirthProfile
import com.example.astro.model.ZodiacSign
import com.example.astro.ui.components.BirthDataInputSection
import com.example.astro.ui.components.HoroscopeChartWheel
import com.example.astro.ui.viewmodel.UiState

// ==========================================
// 1. NATAL CHART SCREEN
// ==========================================
@Composable
fun NatalChartScreen(
    state: UiState<ChartData>,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit,
    onRefresh: () -> Unit
) {
    ChartDetailScaffold(
        title = "Natal Radix Horoscope",
        subtitle = "Placidus House System • Swiss Ephemeris Precision",
        state = state,
        onExportPng = onExportPng,
        onExportPdf = onExportPdf,
        onRefresh = onRefresh
    )
}

// ==========================================
// 2. PROGRESSED CHART SCREEN
// ==========================================
@Composable
fun ProgressedChartScreen(
    state: UiState<ChartData>,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit,
    onRefresh: () -> Unit
) {
    ChartDetailScaffold(
        title = "Secondary Progressions (2026)",
        subtitle = "Solar Day-for-a-Year Progression Model",
        state = state,
        onExportPng = onExportPng,
        onExportPdf = onExportPdf,
        onRefresh = onRefresh
    )
}

// ==========================================
// 3. TRANSITS SCREEN (1-Hour Cache)
// ==========================================
@Composable
fun TransitsScreen(
    state: UiState<ChartData>,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit,
    onRefresh: () -> Unit
) {
    ChartDetailScaffold(
        title = "Current Planetary Transits",
        subtitle = "Transits over Natal Placidus Houses (1-Hour Cache Rule)",
        state = state,
        onExportPng = onExportPng,
        onExportPdf = onExportPdf,
        onRefresh = onRefresh
    )
}

// ==========================================
// 4. HORARY ASTROLOGY SCREEN
// ==========================================
@Composable
fun HoraryScreen(
    state: UiState<Pair<ChartData, HoraryData>>,
    onCastQuestion: (String) -> Unit,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit
) {
    var questionInput by remember { mutableStateOf("Will this prospective undertaking succeed?") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Horary Question Inception",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        label = { Text("Enter Specific Question") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_horary_question")
                    )
                    Button(
                        onClick = { onCastQuestion(questionInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_cast_horary"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cast Horary Chart for This Moment")
                    }
                }
            }
        }

        when (state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val (chart, horary) = state.data
                item {
                    ExportActionHeader(
                        chart = chart,
                        onExportPng = onExportPng,
                        onExportPdf = onExportPdf
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        HoroscopeChartWheel(chartData = chart)
                    }
                }
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Horary Judgment & Significators",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HoraryMetricRow("Planetary Hour Ruler", "${horary.planetaryHourRuler.glyph} ${horary.planetaryHourRuler.planetName}")
                            HoraryMetricRow("Querent (1st House)", "${horary.querentSignificator.glyph} ${horary.querentSignificator.planetName}")
                            HoraryMetricRow("Quesited (7th House)", "${horary.quesitedSignificator.glyph} ${horary.quesitedSignificator.planetName}")
                            HoraryMetricRow("Applying Aspect", horary.applyingAspect)
                            HoraryMetricRow("Essential Dignity", horary.dignityScore)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Final Astrological Judgment:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = horary.horaryJudgment,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ELECTIONAL ASTROLOGY SCREEN
// ==========================================
@Composable
fun ElectionalScreen(
    state: UiState<Pair<ChartData, ElectionalData>>,
    onRecalculate: (String) -> Unit,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit
) {
    var purposeInput by remember { mutableStateOf("Contract Signing & Inception") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Electional Auspicious Window Finder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = purposeInput,
                        onValueChange = { purposeInput = it },
                        label = { Text("Elective Purpose (e.g. Wedding, Launch, LLC)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_electional_purpose")
                    )
                    Button(
                        onClick = { onRecalculate(purposeInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_calculate_election"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Celestial Window")
                    }
                }
            }
        }

        when (state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val (chart, elect) = state.data
                item {
                    ExportActionHeader(chart = chart, onExportPng = onExportPng, onExportPdf = onExportPdf)
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        HoroscopeChartWheel(chartData = chart)
                    }
                }
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auspicious Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${elect.auspiciousScore} / 100", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                            LinearProgressIndicator(
                                progress = { elect.auspiciousScore / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            HoraryMetricRow("Moon Phase", elect.moonPhase)
                            HoraryMetricRow("Void of Course?", if (elect.isMoonVoidOfCourse) "YES (Delay Actions)" else "NO (Clear Passage)")
                            HoraryMetricRow("Planetary Hour", elect.planetaryHour)

                            Text("Favorable Celestial Factors:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            elect.favorableFactors.forEach { factor ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(factor, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Text("Recommendation:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Text(elect.recommendation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. LOCATIONAL ASTROLOGY (ASTROCARTOGRAPHY)
// ==========================================
@Composable
fun AstrocartographyScreen(
    state: UiState<List<AstrocartographyLine>>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Astrocartography Planetary Lines",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Geographical meridians where natal planets coincide with cardinal horizon axes (AC, MC, DC, IC).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        when (state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item { Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error) }
            }
            is UiState.Success -> {
                items(state.data) { line ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(line.planet.glyph, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${line.planet.planetName} on ${line.angle}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(String.format("%.1f°", line.primeLongitude), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text(line.psychologicalTheme, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Key Cities: " + line.citiesInfluenced.joinToString(", "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. MEDICAL ASTROLOGY SCREEN
// ==========================================
@Composable
fun MedicalAstrologyScreen(
    state: UiState<MedicalAstrologyData>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Medical Astrology & Humoral Balance",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Traditional Hippocratic humoral constitution, organ rulerships, and decumbiture equilibrium.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        when (state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item { Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error) }
            }
            is UiState.Success -> {
                val data = state.data
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Dominant Humoral Temperament", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(data.dominantHumor, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Elemental Balance in Natal Radix:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            data.elementDistribution.forEach { (elem, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(elem, style = MaterialTheme.typography.bodySmall)
                                    Text("$count points", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Constitutional Rulerships", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            data.sensitiveBodyParts.forEach { part ->
                                Text("• $part", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Planetary Organ Correspondences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            data.planetaryAilments.forEach { (planet, desc) ->
                                Text("• ${planet.glyph} ${planet.planetName}: $desc", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Healing, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Holistic Balance Protocol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(data.holisticRecommendation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. PROFILE INPUT SCREEN
// ==========================================
@Composable
fun ProfileScreen(
    profile: UserBirthProfile,
    onSaveProfile: (UserBirthProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BirthDataInputSection(
                profile = profile,
                onSaveProfile = onSaveProfile
            )
        }
    }
}

// ==========================================
// COMMON CHART DETAIL SCAFFOLD
// ==========================================
@Composable
fun ChartDetailScaffold(
    title: String,
    subtitle: String,
    state: UiState<ChartData>,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Artistic Flair Header Section
        item {
            val chart = (state as? UiState.Success)?.data
            val sunPlanet = chart?.planets?.firstOrNull { it.planet.name.equals("SUN", ignoreCase = true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (sunPlanet != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Sun in ${sunPlanet.sign.signName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = sunPlanet.formattedPlacement,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier.testTag("btn_refresh_chart")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                }
            }
        }

        when (state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text("Error calculating chart: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val chart = state.data

                // Chart Wheel Container in Artistic Flair style
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        HoroscopeChartWheel(chartData = chart)
                    }
                }

                // Dual Stat Cards (Ascendant & Midheaven) matching Artistic Flair Grid
                item {
                    val (ascSign, ascDeg) = ZodiacSign.fromLongitude(chart.ascendant)
                    val (mcSign, mcDeg) = ZodiacSign.fromLongitude(chart.midheaven)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Ascendant Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Text(
                                        text = "ASCENDANT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${ascSign.signName} ${ascDeg.toInt()}°",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Midheaven Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    )
                                    Text(
                                        text = "MIDHEAVEN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${mcSign.signName} ${mcDeg.toInt()}°",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Synthesis Reading Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Baseline Synthesis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(chart.summaryReading, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Date/Time: ${chart.dateTimeDisplay} • ${chart.locationName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Planetary Placements List Header
                item {
                    Text(
                        "Planetary Placements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(chart.planets) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.planet.glyph, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(p.planet.planetName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("House ${p.house}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(p.formattedPlacement, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("${p.sign.element} • ${p.sign.quality}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                // Export Action Footer matching Artistic Flair
                item {
                    ExportActionHeader(
                        chart = chart,
                        onExportPng = onExportPng,
                        onExportPdf = onExportPdf
                    )
                }
            }
        }
    }
}

@Composable
fun ExportActionHeader(
    chart: ChartData,
    onExportPng: (ChartData) -> Unit,
    onExportPdf: (ChartData) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onExportPdf(chart) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_export_pdf"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = { onExportPng(chart) },
            modifier = Modifier
                .size(48.dp)
                .testTag("btn_export_png"),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Icon(Icons.Default.Share, contentDescription = "Export PNG / Share", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun HoraryMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
