package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SupplierPreferences
import com.example.data.location.GpsLocationClient
import com.example.data.model.AuthLookupResult
import com.example.data.model.DetaillantData
import com.example.data.model.GpsCaptureResult
import com.example.data.model.NetworkResult
import com.example.data.model.StationData
import com.example.data.model.UpdateDetaillantRequest
import com.example.data.model.UpdateStationRequest
import com.example.data.remote.SosHydroApiService
import com.example.data.repository.FournisseurRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
}

sealed class AuthNavigationEvent {
    data class OpenStationForm(val isNotFoundFallback: Boolean) : AuthNavigationEvent()
    data class OpenDetaillantForm(val isNotFoundFallback: Boolean) : AuthNavigationEvent()
    data class ShowError(val message: String) : AuthNavigationEvent()
}

sealed class FormSubmitState {
    data object Idle : FormSubmitState()
    data object CapturingGps : FormSubmitState()
    data class AwaitingGpsConfirmation(val gps: GpsCaptureResult) : FormSubmitState()
    data object Submitting : FormSubmitState()
}

sealed class FormEvent {
    data class SuccessAndReturnHome(val message: String) : FormEvent()
    data class ShowError(val message: String) : FormEvent()
}

class FournisseurViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FournisseurRepository(SosHydroApiService.create())
    private val preferences = SupplierPreferences(application.applicationContext)
    val gpsClient = GpsLocationClient(application.applicationContext)

    val lastStationPhone: StateFlow<String?> = preferences.lastStationPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val lastDetaillantPhone: StateFlow<String?> = preferences.lastDetaillantPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _authEvents = MutableSharedFlow<AuthNavigationEvent>(extraBufferCapacity = 4)
    val authEvents = _authEvents.asSharedFlow()

    private val _activeStationData = MutableStateFlow<StationData?>(null)
    val activeStationData: StateFlow<StationData?> = _activeStationData.asStateFlow()

    private val _activeDetaillantData = MutableStateFlow<DetaillantData?>(null)
    val activeDetaillantData: StateFlow<DetaillantData?> = _activeDetaillantData.asStateFlow()

    private val _prefillPhone = MutableStateFlow("")
    val prefillPhone: StateFlow<String> = _prefillPhone.asStateFlow()

    private val _formSubmitState = MutableStateFlow<FormSubmitState>(FormSubmitState.Idle)
    val formSubmitState: StateFlow<FormSubmitState> = _formSubmitState.asStateFlow()

    private val _formEvents = MutableSharedFlow<FormEvent>(extraBufferCapacity = 4)
    val formEvents = _formEvents.asSharedFlow()

    private val _homeBannerMessage = MutableStateFlow<String?>(null)
    val homeBannerMessage: StateFlow<String?> = _homeBannerMessage.asStateFlow()

    private val _formWarningMessage = MutableStateFlow<String?>(null)
    val formWarningMessage: StateFlow<String?> = _formWarningMessage.asStateFlow()

    fun clearHomeBannerMessage() {
        _homeBannerMessage.value = null
    }

    fun clearFormWarningMessage() {
        _formWarningMessage.value = null
    }

    fun startNewStationRegistration(prefill: String = "") {
        _activeStationData.value = null
        _prefillPhone.value = prefill.trim()
        _formWarningMessage.value = null
        _formSubmitState.value = FormSubmitState.Idle
    }

    fun startNewDetaillantRegistration(prefill: String = "") {
        _activeDetaillantData.value = null
        _prefillPhone.value = prefill.trim()
        _formWarningMessage.value = null
        _formSubmitState.value = FormSubmitState.Idle
    }

    fun verifyStationPhone(telephone: String) {
        val cleaned = telephone.trim()
        if (cleaned.isEmpty() || _authUiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            preferences.saveLastStationPhone(cleaned)

            when (val result = repository.verifyStationPhone(cleaned)) {
                is NetworkResult.Success -> {
                    _authUiState.value = AuthUiState.Idle
                    when (val lookup = result.value) {
                        is AuthLookupResult.Found -> {
                            _activeStationData.value = lookup.data
                            _prefillPhone.value = lookup.data.telephone.ifBlank { cleaned }
                            _formWarningMessage.value = null
                            _formSubmitState.value = FormSubmitState.Idle
                            _authEvents.emit(AuthNavigationEvent.OpenStationForm(isNotFoundFallback = false))
                        }
                        AuthLookupResult.NotFound -> {
                            _activeStationData.value = null
                            _prefillPhone.value = cleaned
                            _formWarningMessage.value = "Numéro inconnu : Procédez au premier enregistrement GPS."
                            _formSubmitState.value = FormSubmitState.Idle
                            _authEvents.emit(AuthNavigationEvent.OpenStationForm(isNotFoundFallback = true))
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _authUiState.value = AuthUiState.Idle
                    _authEvents.emit(AuthNavigationEvent.ShowError(result.message))
                }
            }
        }
    }

    fun verifyDetaillantPhone(telephone: String) {
        val cleaned = telephone.trim()
        if (cleaned.isEmpty() || _authUiState.value is AuthUiState.Loading) return

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            preferences.saveLastDetaillantPhone(cleaned)

            when (val result = repository.verifyDetaillantPhone(cleaned)) {
                is NetworkResult.Success -> {
                    _authUiState.value = AuthUiState.Idle
                    when (val lookup = result.value) {
                        is AuthLookupResult.Found -> {
                            _activeDetaillantData.value = lookup.data
                            _prefillPhone.value = lookup.data.telephone.ifBlank { cleaned }
                            _formWarningMessage.value = null
                            _formSubmitState.value = FormSubmitState.Idle
                            _authEvents.emit(AuthNavigationEvent.OpenDetaillantForm(isNotFoundFallback = false))
                        }
                        AuthLookupResult.NotFound -> {
                            _activeDetaillantData.value = null
                            _prefillPhone.value = cleaned
                            _formWarningMessage.value = "Numéro non enregistré. Créez votre boutique ci-dessous."
                            _formSubmitState.value = FormSubmitState.Idle
                            _authEvents.emit(AuthNavigationEvent.OpenDetaillantForm(isNotFoundFallback = true))
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _authUiState.value = AuthUiState.Idle
                    _authEvents.emit(AuthNavigationEvent.ShowError(result.message))
                }
            }
        }
    }

    fun captureGpsForConfirmation() {
        if (_formSubmitState.value != FormSubmitState.Idle) return

        viewModelScope.launch {
            _formSubmitState.value = FormSubmitState.CapturingGps
            val captureResult = gpsClient.captureHighPrecisionLocation()
            captureResult.fold(
                onSuccess = { gps ->
                    _formSubmitState.value = FormSubmitState.AwaitingGpsConfirmation(gps)
                },
                onFailure = { error ->
                    _formSubmitState.value = FormSubmitState.Idle
                    _formEvents.emit(
                        FormEvent.ShowError(error.localizedMessage ?: "Erreur GPS inconnue")
                    )
                }
            )
        }
    }

    fun cancelGpsConfirmation() {
        _formSubmitState.value = FormSubmitState.Idle
    }

    fun submitStationUpdate(
        stationId: String,
        nomStation: String,
        lieuDit: String,
        telephone: String,
        estOuvert: Boolean,
        prixEssence: String,
        dispoEssence: Boolean,
        prixGazoil: String,
        dispoGazoil: Boolean,
        prixPetrole: String,
        dispoPetrole: Boolean,
        prixGaz12kg: String,
        dispoGaz12kg: Boolean,
        prixGaz35kg: String,
        dispoGaz35kg: Boolean,
        gps: GpsCaptureResult
    ) {
        viewModelScope.launch {
            _formSubmitState.value = FormSubmitState.Submitting
            val cleanedPhone = telephone.trim()
            if (cleanedPhone.isNotEmpty()) {
                preferences.saveLastStationPhone(cleanedPhone)
            }

            val request = UpdateStationRequest(
                stationId = stationId,
                nomStation = nomStation,
                lieuDit = lieuDit.trim(),
                telephone = cleanedPhone,
                latitude = gps.latitude.toString(),
                longitude = gps.longitude.toString(),
                estOuvert = if (estOuvert) "1" else "0",
                prixEssence = prixEssence.trim().ifEmpty { "0" },
                dispoEssence = if (dispoEssence) "1" else "0",
                prixGazoil = prixGazoil.trim().ifEmpty { "0" },
                dispoGazoil = if (dispoGazoil) "1" else "0",
                prixPetrole = prixPetrole.trim().ifEmpty { "0" },
                dispoPetrole = if (dispoPetrole) "1" else "0",
                prixGaz12kg = prixGaz12kg.trim().ifEmpty { "0" },
                dispoGaz12kg = if (dispoGaz12kg) "1" else "0",
                prixGaz35kg = prixGaz35kg.trim().ifEmpty { "0" },
                dispoGaz35kg = if (dispoGaz35kg) "1" else "0"
            )

            when (val result = repository.submitStation(request)) {
                is NetworkResult.Success -> {
                    _formSubmitState.value = FormSubmitState.Idle
                    _homeBannerMessage.value = result.value
                    _formEvents.emit(FormEvent.SuccessAndReturnHome(result.value))
                }
                is NetworkResult.Error -> {
                    _formSubmitState.value = FormSubmitState.Idle
                    _formEvents.emit(FormEvent.ShowError(result.message))
                }
            }
        }
    }

    fun submitDetaillantUpdate(
        detaillantId: String,
        nomBoutique: String,
        nomGerant: String,
        quartier: String,
        telephone: String,
        prix6kg: String,
        prix12kg: String,
        enStock: Boolean,
        gps: GpsCaptureResult
    ) {
        viewModelScope.launch {
            _formSubmitState.value = FormSubmitState.Submitting
            val cleanedPhone = telephone.trim()
            if (cleanedPhone.isNotEmpty()) {
                preferences.saveLastDetaillantPhone(cleanedPhone)
            }

            val request = UpdateDetaillantRequest(
                detaillantId = detaillantId,
                nomBoutique = nomBoutique.trim(),
                nomGerant = nomGerant.trim(),
                quartier = quartier.trim(),
                telephone = cleanedPhone,
                latitude = gps.latitude.toString(),
                longitude = gps.longitude.toString(),
                prix6kg = prix6kg.trim().ifEmpty { "0" },
                prix12kg = prix12kg.trim().ifEmpty { "0" },
                enStock = if (enStock) "1" else "0"
            )

            when (val result = repository.submitDetaillant(request)) {
                is NetworkResult.Success -> {
                    _formSubmitState.value = FormSubmitState.Idle
                    _homeBannerMessage.value = result.value
                    _formEvents.emit(FormEvent.SuccessAndReturnHome(result.value))
                }
                is NetworkResult.Error -> {
                    _formSubmitState.value = FormSubmitState.Idle
                    _formEvents.emit(FormEvent.ShowError(result.message))
                }
            }
        }
    }
}
