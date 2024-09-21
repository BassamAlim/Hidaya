package bassamalim.hidaya.features.remembrances.reader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.remembrances.reader.domain.RemembranceReaderDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemembranceReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: RemembranceReaderDomain
): ViewModel() {

    private val id = savedStateHandle.get<Int>("remembrance_id") ?: 0

    private lateinit var language: Language

    private val _uiState = MutableStateFlow(RemembranceReaderUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getTextSize()
    ) { state, textSize -> state.copy(
        textSize = textSize,
    )}.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = RemembranceReaderUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()

            _uiState.update { it.copy(
                title = domain.getTitle(id, language),
                items = getItems()
            )}
        }
    }

    private suspend fun getItems(): List<RemembrancePassage> {
        val remembrancePassages = domain.getRemembrancePassages(id)
        return remembrancePassages.filter {
            !(language == Language.ENGLISH && it.textEn.isNullOrEmpty())
        }.map { passage ->
            when (language) {
                Language.ARABIC -> {
                    RemembrancePassage(
                        id = passage.id,
                        title = passage.titleAr,
                        text = passage.textAr!!,
                        virtue = passage.virtueAr,
                        reference = passage.referenceAr,
                        repetition = passage.repetitionAr,
                        isTitleAvailable = isTitleAvailable(passage.titleAr),
                        isTranslationAvailable = false,
                        isVirtueAvailable = isVirtueAvailable(passage.virtueAr),
                        isReferenceAvailable = isReferenceAvailable(passage.referenceAr),
                        isRepetitionAvailable = isRepetitionAvailable(passage.repetitionAr)
                    )
                }
                Language.ENGLISH -> {
                    RemembrancePassage(
                        id = passage.id,
                        title = passage.titleEn,
                        text = passage.textEn!!,
                        translation = passage.textEnTranslation,
                        virtue = passage.virtueEn,
                        reference = passage.referenceEn,
                        repetition = passage.repetitionEn,
                        isTitleAvailable = isTitleAvailable(passage.titleEn),
                        isTranslationAvailable = isTranslationAvailable(passage.textEnTranslation),
                        isVirtueAvailable = isVirtueAvailable(passage.virtueEn),
                        isReferenceAvailable = isReferenceAvailable(passage.referenceEn),
                        isRepetitionAvailable = isRepetitionAvailable(passage.repetitionEn)
                    )
                }
            }
        }
    }

    fun onTextSizeSliderChange(textSize: Float) {
        viewModelScope.launch {
            domain.setTextSize(textSize)
        }
    }

    fun onInfoClick(text: String) {
        _uiState.update { it.copy(
            isInfoDialogShown = true,
            infoDialogText = text
        )}
    }

    fun onInfoDialogDismiss() {
        _uiState.update { it.copy(
            isInfoDialogShown = false
        )}
    }

    private fun isTitleAvailable(title: String?) = !title.isNullOrEmpty()

    private fun isTranslationAvailable(translation: String?) =
        language != Language.ARABIC && !translation.isNullOrEmpty()

    private fun isVirtueAvailable(virtue: String?) = !virtue.isNullOrEmpty()

    private fun isReferenceAvailable(reference: String?) = !reference.isNullOrEmpty()

    private fun isRepetitionAvailable(repetition: String) = repetition != "1"

}