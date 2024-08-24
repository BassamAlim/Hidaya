package bassamalim.hidaya.features.supplicationsReader

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
class SupplicationsReaderViewModel @Inject constructor(
    private val repo: SupplicationsReaderRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val id = savedStateHandle.get<Int>("thikr_id") ?: 0

    private val language = repo.getLanguage()

    private val _uiState = MutableStateFlow(SupplicationsReaderState(
        title = repo.getTitle(id),
        textSize = repo.getTextSize(),
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<RemembrancePassage> {
        val thikrParts = repo.getThikrParts(id)

        val items = mutableListOf<RemembrancePassage>()
        for (i in thikrParts.indices) {
            val t = thikrParts[i]

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