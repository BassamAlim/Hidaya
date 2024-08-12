package bassamalim.hidaya.features.supplicationsReader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Thikr
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

    private fun getItems(): List<Thikr> {
        val thikrParts = repo.getThikrParts(id)

        val items = ArrayList<Thikr>()
        for (i in thikrParts.indices) {
            val t = thikrParts[i]

            if (language == Language.ENGLISH && t.textEn.isNullOrEmpty()) continue

            if (language == Language.ENGLISH)
                items.add(
                    Thikr(
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
                    Thikr(
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

    fun shouldShowTitle(thikr: Thikr) = !thikr.title.isNullOrEmpty()

    fun shouldShowTranslation(thikr: Thikr) =
        language != Language.ARABIC && !thikr.textTranslation.isNullOrEmpty()

    fun shouldShowFadl(thikr: Thikr) = !thikr.fadl.isNullOrEmpty()

    fun shouldShowReference(thikr: Thikr) = !thikr.reference.isNullOrEmpty()

    fun shouldShowRepetition(thikr: Thikr) = thikr.repetition != "1"

}