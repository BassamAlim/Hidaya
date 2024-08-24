package bassamalim.hidaya.features.remembranceReader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.RemembrancePassage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RemembranceReaderViewModel @Inject constructor(
    private val repo: RemembranceReaderRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val id = savedStateHandle.get<Int>("remembrance_id") ?: 0

    private val language = repo.getLanguage()

    private val _uiState = MutableStateFlow(RemembranceReaderState(
        title = repo.getTitle(id),
        textSize = repo.getTextSize(),
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<RemembrancePassage> {
        val remembrancePassages = repo.getRemembrancePassages(id)

        val items = mutableListOf<RemembrancePassage>()
        for (i in remembrancePassages.indices) {
            val t = remembrancePassages[i]

            if (language == Language.ENGLISH && t.textEn.isNullOrEmpty()) continue

            if (language == Language.ENGLISH)
                items.add(
                    RemembrancePassage(
                        id = t.id,
                        title = t.titleEn,
                        text = t.textEn!!,
                        textTranslation = t.textEnTranslation,
                        fadl = t.virtueEn,
                        reference = t.referenceEn,
                        repetition = t.repetitionEn
                    )
                )
            else
                items.add(
                    RemembrancePassage(
                        id = t.id,
                        title = t.titleAr,
                        text = t.textAr!!,
                        textTranslation = t.textEnTranslation,
                        fadl = t.virtueAr,
                        reference = t.referenceAr,
                        repetition = t.repetitionAr
                    )
                )
        }
        return items
    }

    fun onTextSizeChange(textSize: Float) {
        _uiState.update { it.copy(
            textSize = textSize
        )}

        repo.setTextSize(textSize)
    }

    fun showInfoDialog(text: String) {
        _uiState.update { it.copy(
            infoDialogShown = true,
            infoDialogText = text
        )}
    }

    fun onInfoDialogDismiss() {
        _uiState.update { it.copy(
            infoDialogShown = false
        )}
    }

    fun shouldShowTitle(remembrancePassage: RemembrancePassage) = !remembrancePassage.title.isNullOrEmpty()

    fun shouldShowTranslation(remembrancePassage: RemembrancePassage) =
        language != Language.ARABIC && !remembrancePassage.textTranslation.isNullOrEmpty()

    fun shouldShowFadl(remembrancePassage: RemembrancePassage) = !remembrancePassage.fadl.isNullOrEmpty()

    fun shouldShowReference(remembrancePassage: RemembrancePassage) = !remembrancePassage.reference.isNullOrEmpty()

    fun shouldShowRepetition(remembrancePassage: RemembrancePassage) = remembrancePassage.repetition != "1"

}