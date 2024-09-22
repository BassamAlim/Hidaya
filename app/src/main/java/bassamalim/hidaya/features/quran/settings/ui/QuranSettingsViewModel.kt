package bassamalim.hidaya.features.quran.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import bassamalim.hidaya.features.quran.settings.domain.QuranSettingsDomain
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
class QuranSettingsViewModel @Inject constructor(
    private val domain: QuranSettingsDomain
): ViewModel() {

    lateinit var numeralsLanguage: Language
    lateinit var reciterNames: List<String>
    lateinit var reciterIds: Array<Int>

    private val _uiState = MutableStateFlow(QuranSettingsUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuranSettingsUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            reciterNames = domain.getReciterNames()
            reciterIds = Array(size = reciterNames.size) { idx -> idx }

            _uiState.update { it.copy(
                isLoading = false,
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