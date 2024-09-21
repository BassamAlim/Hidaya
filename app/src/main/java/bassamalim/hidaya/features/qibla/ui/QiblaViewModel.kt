package bassamalim.hidaya.features.qibla.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.qibla.domain.QiblaDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val domain: QiblaDomain
): ViewModel() {

    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QiblaUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage().first()

            domain.initialize(
                updateAccuracy = { newAccuracy ->
                    _uiState.update { it.copy(
                        accuracy = newAccuracy
                    )}
                },
                showUnsupported = {
                    _uiState.update { it.copy(
                        error = true,
                        errorMassageResId = R.string.feature_not_supported
                    )}
                },
                adjustQiblaDial = { target ->
                    _uiState.update { it.copy(
                        qiblaAngle = target,
                        isOnPoint = target > -2 && target < 2
                    )}
                },
                adjustNorthDial = { compassAngle ->
                    _uiState.update { it.copy(
                        compassAngle = compassAngle
                    )}
                }
            )
        }

        if (domain.location != null) {
            _uiState.update { it.copy(
                distanceToKaaba = translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = domain.getDistance().toString()
                )
            )}
        }
        else {
            _uiState.update { it.copy(
                error = true,
                errorMassageResId = R.string.location_permission_for_qibla
            )}
        }
    }

    fun onStart() {
        domain.startCompass()
    }

    fun onStop() {
        domain.stopCompass()
    }

    fun onAccuracyIndicatorClick() {
        _uiState.update { it.copy(
            calibrationDialogShown = true
        )}
    }

    fun onCalibrationDialogDismiss() {
        _uiState.update { it.copy(
            calibrationDialogShown = false
        )}
    }

}