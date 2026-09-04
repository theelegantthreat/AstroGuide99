package com.example.astro.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.astro.data.local.AppDatabase
import com.example.astro.data.local.UserPreferencesDataStore
import com.example.astro.data.repository.AstrologyRepository
import com.example.astro.export.AstrologyPdfReportGenerator
import com.example.astro.export.ChartImageExporter
import com.example.astro.model.AstrocartographyLine
import com.example.astro.model.ChartData
import com.example.astro.model.ElectionalData
import com.example.astro.model.HoraryData
import com.example.astro.model.MedicalAstrologyData
import com.example.astro.model.UserBirthProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class AstroScreen(val title: String, val subtitle: String, val iconName: String) {
    NATAL("Natal Chart", "Radix Birth Horoscope", "Star"),
    PROGRESSED("Progressed Chart", "Day-for-a-Year Progression", "Timeline"),
    TRANSITS("Transits", "Current Planetary Weather", "Sync"),
    HORARY("Horary Astrology", "Moment of the Question", "Help"),
    ELECTIONAL("Electional Astrology", "Auspicious Timing Selection", "Event"),
    LOCATIONAL("Locational Astrology", "Astrocartography Power Lines", "Map"),
    MEDICAL("Medical Astrology", "Humoral Temperament & Organs", "Healing"),
    ORACLE("AI Oracle & Cloud", "Gemini AI, Grounding & Firestore", "AutoAwesome"),
    PROFILE("Birth Input Profile", "Edit Birth Date, Time & Coordinates", "Person")
}

class AstrologyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AstrologyRepository

    init {
        val db = AppDatabase.getInstance(application)
        val dataStore = UserPreferencesDataStore(application)
        repository = AstrologyRepository(db, dataStore)
    }

    val userProfile: StateFlow<UserBirthProfile> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserBirthProfile()
        )

    private val _currentScreen = MutableStateFlow(AstroScreen.NATAL)
    val currentScreen: StateFlow<AstroScreen> = _currentScreen.asStateFlow()

    private val _natalChartState = MutableStateFlow<UiState<ChartData>>(UiState.Loading)
    val natalChartState: StateFlow<UiState<ChartData>> = _natalChartState.asStateFlow()

    private val _progressedChartState = MutableStateFlow<UiState<ChartData>>(UiState.Loading)
    val progressedChartState: StateFlow<UiState<ChartData>> = _progressedChartState.asStateFlow()

    private val _transitsChartState = MutableStateFlow<UiState<ChartData>>(UiState.Loading)
    val transitsChartState: StateFlow<UiState<ChartData>> = _transitsChartState.asStateFlow()

    private val _horaryState = MutableStateFlow<UiState<Pair<ChartData, HoraryData>>>(UiState.Loading)
    val horaryState: StateFlow<UiState<Pair<ChartData, HoraryData>>> = _horaryState.asStateFlow()

    private val _electionalState = MutableStateFlow<UiState<Pair<ChartData, ElectionalData>>>(UiState.Loading)
    val electionalState: StateFlow<UiState<Pair<ChartData, ElectionalData>>> = _electionalState.asStateFlow()

    private val _astrocartographyState = MutableStateFlow<UiState<List<AstrocartographyLine>>>(UiState.Loading)
    val astrocartographyState: StateFlow<UiState<List<AstrocartographyLine>>> = _astrocartographyState.asStateFlow()

    private val _medicalState = MutableStateFlow<UiState<MedicalAstrologyData>>(UiState.Loading)
    val medicalState: StateFlow<UiState<MedicalAstrologyData>> = _medicalState.asStateFlow()

    init {
        viewModelScope.launch {
            userProfile.collect { profile ->
                loadAllCalculations(profile)
            }
        }
    }

    fun setScreen(screen: AstroScreen) {
        _currentScreen.value = screen
    }

    fun updateUserProfile(profile: UserBirthProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            loadAllCalculations(profile, forceRefresh = true)
        }
    }

    fun loadAllCalculations(profile: UserBirthProfile, forceRefresh: Boolean = false) {
        loadNatal(profile, forceRefresh)
        loadProgressed(profile, forceRefresh)
        loadTransits(profile, forceRefresh)
        loadHorary("Will the prospective enterprise succeed?", profile)
        loadElectional("Contract Signing & Inception", profile)
        loadAstrocartography(profile)
        loadMedicalAstrology(profile)
    }

    fun loadNatal(profile: UserBirthProfile, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _natalChartState.value = UiState.Loading
            repository.getNatalChart(profile, forceRefresh)
                .catch { e -> _natalChartState.value = UiState.Error(e.localizedMessage ?: "Failed to calculate natal chart") }
                .collect { chart -> _natalChartState.value = UiState.Success(chart) }
        }
    }

    fun loadProgressed(profile: UserBirthProfile, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _progressedChartState.value = UiState.Loading
            repository.getProgressedChart(profile, currentYear = 2026, forceRefresh)
                .catch { e -> _progressedChartState.value = UiState.Error(e.localizedMessage ?: "Failed to calculate progressed chart") }
                .collect { chart -> _progressedChartState.value = UiState.Success(chart) }
        }
    }

    fun loadTransits(profile: UserBirthProfile, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _transitsChartState.value = UiState.Loading
            repository.getTransits(profile, forceRefresh)
                .catch { e -> _transitsChartState.value = UiState.Error(e.localizedMessage ?: "Failed to calculate transits") }
                .collect { chart -> _transitsChartState.value = UiState.Success(chart) }
        }
    }

    fun loadHorary(question: String, profile: UserBirthProfile) {
        viewModelScope.launch {
            _horaryState.value = UiState.Loading
            repository.getHoraryChart(question, profile.latitude, profile.longitude, profile.cityName)
                .catch { e -> _horaryState.value = UiState.Error(e.localizedMessage ?: "Failed to cast horary chart") }
                .collect { result -> _horaryState.value = UiState.Success(result) }
        }
    }

    fun loadElectional(purpose: String, profile: UserBirthProfile) {
        viewModelScope.launch {
            _electionalState.value = UiState.Loading
            repository.getElectionalChart(
                purpose, 2026, 9, 15, 11, 30,
                profile.latitude, profile.longitude, profile.cityName
            )
                .catch { e -> _electionalState.value = UiState.Error(e.localizedMessage ?: "Failed to compute electional chart") }
                .collect { result -> _electionalState.value = UiState.Success(result) }
        }
    }

    fun loadAstrocartography(profile: UserBirthProfile) {
        viewModelScope.launch {
            _astrocartographyState.value = UiState.Loading
            repository.getAstrocartography(profile)
                .catch { e -> _astrocartographyState.value = UiState.Error(e.localizedMessage ?: "Failed to compute astrocartography") }
                .collect { lines -> _astrocartographyState.value = UiState.Success(lines) }
        }
    }

    fun loadMedicalAstrology(profile: UserBirthProfile) {
        viewModelScope.launch {
            _medicalState.value = UiState.Loading
            repository.getMedicalAstrology(profile)
                .catch { e -> _medicalState.value = UiState.Error(e.localizedMessage ?: "Failed to compute medical astrology") }
                .collect { data -> _medicalState.value = UiState.Success(data) }
        }
    }

    fun exportPng(context: Context, chart: ChartData) {
        viewModelScope.launch {
            ChartImageExporter.exportAndShareChartPng(context, chart)
        }
    }

    fun exportPdf(context: Context, chart: ChartData) {
        viewModelScope.launch {
            AstrologyPdfReportGenerator.generateAndSharePdfReport(context, chart)
        }
    }
}
