package bassamalim.hidaya.features.athkarViewer

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
class AthkarViewerVM @Inject constructor(
    private val repo: AthkarViewerRepo,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val id = savedStateHandle.get<Int>("thikr_id") ?: 0

    private val language = repo.getLanguage()

    private val _uiState = MutableStateFlow(
        AthkarViewerState(
            title = repo.getTitle(id),
            textSize = repo.getTextSize(),
            items = getItems()
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<Thikr> {
        val thikrs = repo.getThikrs(id)

        val items = ArrayList<Thikr>()
        for (i in thikrs.indices) {
            val t = thikrs[i]

            if (language == Language.ENGLISH && t.textEn.isNullOrEmpty()) continue

            if (language == Language.ENGLISH)
                items.add(
                    Thikr(
                        t.thikrId, t.titleEn, t.textEn!!, t.textEnTranslation,
                        t.fadlEn, t.referenceEn, t.repetitionEn
                    )
                )
            else
                items.add(
                    Thikr(t.thikrId, t.title, t.text!!,
                        t.textEnTranslation, t.fadl, t.reference,
                        t.repetition
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

    fun shouldShowTitle(thikr: Thikr): Boolean {
        return !thikr.title.isNullOrEmpty()
    }

    fun shouldShowTranslation(thikr: Thikr): Boolean {
        return language != Language.ARABIC
                && !thikr.textTranslation.isNullOrEmpty()
    }

    fun shouldShowFadl(thikr: Thikr): Boolean {
        return !thikr.fadl.isNullOrEmpty()
    }

    fun shouldShowReference(thikr: Thikr): Boolean {
        return !thikr.reference.isNullOrEmpty()
    }

    fun shouldShowRepetition(thikr: Thikr): Boolean {
        return thikr.repetition != "1"
    }

}