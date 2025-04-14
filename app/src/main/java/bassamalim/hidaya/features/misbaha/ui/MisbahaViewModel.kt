package bassamalim.hidaya.features.misbaha.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.misbaha.domain.MisbahaDomain
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
class MisbahaViewModel @Inject constructor(
    val domain: MisbahaDomain
): ViewModel() {

    private lateinit var numeralsLanguage: Language
    var count = 0
        private set

    private val _uiState = MutableStateFlow(MisbahaUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = MisbahaUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage().first()
            _uiState.update { it.copy(
                isLoading = false,
                countText = translateNums(
                    string = count.toString(),
                    numeralsLanguage = numeralsLanguage
                )
            )}
        }
    }

    fun onIncrementClick() {
        count++

        updateCountText()
    }

    fun onResetClick() {
        count = 0

        updateCountText()
    }

    private fun updateCountText() {
        _uiState.update { it.copy(
            countText = translateNums(
                string = count.toString(),
                numeralsLanguage = numeralsLanguage
            )
        )}
    }

}