package bassamalim.hidaya.features.quran.quranSettings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.quranReader.ui.QuranViewType
import bassamalim.hidaya.features.quran.quranSettings.domain.QuranSettingsDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranSettingsViewModel @Inject constructor(
    private val domain: QuranSettingsDomain
): ViewModel() {

    lateinit var numeralsLanguage: Language
    val reciterNames = domain.getReciterNames()
    val reciterIds = Array(size = reciterNames.size) { idx -> idx }

    private val _uiState = MutableStateFlow(QuranSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                viewType = domain.getViewType().first(),
                textSize = domain.getTextSize().first(),
                reciterId = domain.getReciterId().first(),
                repeatMode = domain.getRepeatMode().first(),
                shouldStopOnSuraEnd = domain.getShouldStopOnSuraEnd().first(),
                shouldStopOnPageEnd = domain.getShouldStopOnPageEnd().first()
            )}
        }
    }

    fun onViewTypeChange(viewType: QuranViewType) {
        _uiState.update { it.copy(
            viewType = viewType
        )}
    }

    fun onTextSizeChange(size: Float) {
        _uiState.update { it.copy(
            textSize = size
        )}
    }

    fun onReciterChange(reciterId: Int) {
        _uiState.update { it.copy(
            reciterId = reciterId
        )}
    }

    fun onRepeatModeChange(repeatMode: VerseRepeatMode) {
        _uiState.update { it.copy(
            repeatMode = repeatMode
        )}
    }

    fun onShouldStopOnSuraEndChange(shouldStop: Boolean) {
        _uiState.update { it.copy(
            shouldStopOnSuraEnd = shouldStop
        )}
    }

    fun onShouldStopOnPageEndChange(shouldStop: Boolean) {
        _uiState.update { it.copy(
            shouldStopOnPageEnd = shouldStop
        )}
    }

    fun onCancel(mainOnDone: () -> Unit) {
        mainOnDone()
    }

    fun onSave(mainOnDone: () -> Unit) {
        viewModelScope.launch {
            domain.setViewType(uiState.value.viewType)
            domain.setTextSize(uiState.value.textSize)
            domain.setReciterId(uiState.value.reciterId)
            domain.setRepeatMode(uiState.value.repeatMode)
            domain.setShouldStopOnSuraEnd(uiState.value.shouldStopOnSuraEnd)
            domain.setShouldStopOnPageEnd(uiState.value.shouldStopOnPageEnd)

            mainOnDone()
        }
    }

}