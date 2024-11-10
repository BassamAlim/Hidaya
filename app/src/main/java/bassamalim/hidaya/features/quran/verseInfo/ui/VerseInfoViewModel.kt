package bassamalim.hidaya.features.quran.verseInfo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.quran.verseInfo.domain.VerseInfoDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerseInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: VerseInfoDomain,
    private val navigator: Navigator
): ViewModel() {

    private val verseId = savedStateHandle.get<Int>("verse_id")!!

    private val _uiState = MutableStateFlow(VerseInfoUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = VerseInfoUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            val verse = domain.getVerse(verseId)

            _uiState.update { it.copy(
                isLoading = false,
                interpretation = verse.interpretation
            )}
        }
    }

    fun onDismiss() {
        navigator.popBackStack()
    }

}