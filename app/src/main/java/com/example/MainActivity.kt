package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.astro.model.ChartData
import com.example.astro.ui.screens.AstrocartographyScreen
import com.example.astro.ui.screens.ElectionalScreen
import com.example.astro.ui.screens.HoraryScreen
import com.example.astro.ui.screens.MedicalAstrologyScreen
import com.example.astro.ui.screens.NatalChartScreen
import com.example.astro.ui.screens.OracleScreen
import com.example.astro.ui.screens.ProfileScreen
import com.example.astro.ui.screens.ProgressedChartScreen
import com.example.astro.ui.screens.TransitsScreen
import com.example.astro.ui.viewmodel.AstroScreen
import com.example.astro.ui.viewmodel.AstrologyViewModel
import com.example.astro.ui.viewmodel.UiState
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AstroGuideApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroGuideApp(viewModel: AstrologyViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val natalState by viewModel.natalChartState.collectAsState()
    val progressedState by viewModel.progressedChartState.collectAsState()
    val transitsState by viewModel.transitsChartState.collectAsState()
    val horaryState by viewModel.horaryState.collectAsState()
    val electionalState by viewModel.electionalState.collectAsState()
    val astrocartographyState by viewModel.astrocartographyState.collectAsState()
    val medicalState by viewModel.medicalState.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .testTag("navigation_drawer_sheet"),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Celestial Header in Artistic Flair styling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "☉",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onTertiary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "AstroGuide99",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Swiss Ephemeris • Placidus",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Seeker: ${userProfile.fullName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "${userProfile.birthDateDisplay} • ${userProfile.cityName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 7 Astrological Calculation Variants + AI + Profile
                    AstroScreen.entries.forEach { screen ->
                        val selected = currentScreen == screen
                        val icon = getScreenIcon(screen)
                        NavigationDrawerItem(
                            label = {
                                Column {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = screen.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            },
                            selected = selected,
                            onClick = {
                                viewModel.setScreen(screen)
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag("nav_item_${screen.name.lowercase()}"),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AstroGuide99 v1.0 • Offline-First Native Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AstroGuide99",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = currentScreen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_hamburger_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Astrological Navigation Drawer",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        val initials = userProfile.fullName.trim().split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                            .ifEmpty { "JD" }
                        IconButton(
                            onClick = { viewModel.setScreen(AstroScreen.PROFILE) },
                            modifier = Modifier.testTag("btn_top_profile")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AstroScreen.NATAL -> NatalChartScreen(
                        state = natalState,
                        onExportPng = { chart -> viewModel.exportPng(context, chart) },
                        onExportPdf = { chart -> viewModel.exportPdf(context, chart) },
                        onRefresh = { viewModel.loadNatal(userProfile, forceRefresh = true) }
                    )
                    AstroScreen.PROGRESSED -> ProgressedChartScreen(
                        state = progressedState,
                        onExportPng = { chart -> viewModel.exportPng(context, chart) },
                        onExportPdf = { chart -> viewModel.exportPdf(context, chart) },
                        onRefresh = { viewModel.loadProgressed(userProfile, forceRefresh = true) }
                    )
                    AstroScreen.TRANSITS -> TransitsScreen(
                        state = transitsState,
                        onExportPng = { chart -> viewModel.exportPng(context, chart) },
                        onExportPdf = { chart -> viewModel.exportPdf(context, chart) },
                        onRefresh = { viewModel.loadTransits(userProfile, forceRefresh = true) }
                    )
                    AstroScreen.HORARY -> HoraryScreen(
                        state = horaryState,
                        onCastQuestion = { q -> viewModel.loadHorary(q, userProfile) },
                        onExportPng = { chart -> viewModel.exportPng(context, chart) },
                        onExportPdf = { chart -> viewModel.exportPdf(context, chart) }
                    )
                    AstroScreen.ELECTIONAL -> ElectionalScreen(
                        state = electionalState,
                        onRecalculate = { p -> viewModel.loadElectional(p, userProfile) },
                        onExportPng = { chart -> viewModel.exportPng(context, chart) },
                        onExportPdf = { chart -> viewModel.exportPdf(context, chart) }
                    )
                    AstroScreen.LOCATIONAL -> AstrocartographyScreen(
                        state = astrocartographyState
                    )
                    AstroScreen.MEDICAL -> MedicalAstrologyScreen(
                        state = medicalState
                    )
                    AstroScreen.ORACLE -> {
                        val currentNatal = (natalState as? UiState.Success<ChartData>)?.data
                        OracleScreen(
                            profile = userProfile,
                            natalChart = currentNatal
                        )
                    }
                    AstroScreen.PROFILE -> ProfileScreen(
                        profile = userProfile,
                        onSaveProfile = { newProfile -> viewModel.updateUserProfile(newProfile) }
                    )
                }
            }
        }
    }
}

fun getScreenIcon(screen: AstroScreen): ImageVector {
    return when (screen) {
        AstroScreen.NATAL -> Icons.Default.Star
        AstroScreen.PROGRESSED -> Icons.Default.Timeline
        AstroScreen.TRANSITS -> Icons.Default.Sync
        AstroScreen.HORARY -> Icons.Default.Help
        AstroScreen.ELECTIONAL -> Icons.Default.Event
        AstroScreen.LOCATIONAL -> Icons.Default.Map
        AstroScreen.MEDICAL -> Icons.Default.Healing
        AstroScreen.ORACLE -> Icons.Default.AutoAwesome
        AstroScreen.PROFILE -> Icons.Default.Person
    }
}
