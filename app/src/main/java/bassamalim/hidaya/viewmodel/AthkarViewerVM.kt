package bassamalim.hidaya.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.models.Thikr
import bassamalim.hidaya.repository.AthkarViewerRepo
import bassamalim.hidaya.state.AthkarViewerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AthkarViewerVM @Inject constructor(
    private val repository: AthkarViewerRepo,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val id = savedStateHandle.get<Int>("thikr_id") ?: 0

    private val _uiState = MutableStateFlow(AthkarViewerState(
        title = repository.getTitle(id),
        language = repository.language,
        textSize = repository.getTextSize().toFloat(),
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<Thikr> {
        val thikrs = repository.getThikrs(id)

        val items = ArrayList<Thikr>()
        for (i in thikrs.indices) {
            val t = thikrs[i]

            if (repository.language == Language.ENGLISH &&
                (t.getTextEn() == null || t.getTextEn()!!.isEmpty()))
                continue

            if (repository.language == Language.ENGLISH)
                items.add(
                    Thikr(
                        t.getThikrId(), t.getTitleEn(), t.getTextEn()!!, t.getTextEnTranslation(),
                        t.getFadlEn(), t.getReferenceEn(), t.getRepetitionEn()
                    )
                )
            else
                items.add(
                    Thikr(t.getThikrId(), t.getTitle(), t.getText()!!,
                        t.getTextEnTranslation(), t.getFadl(), t.getReference(),
                        t.getRepetition()
                    )
                )
        }
        return items
    }

    fun onTextSizeChange(textSize: Float) {
        _uiState.update { it.copy(
            textSize = textSize
        )}

        repository.updateTextSize(textSize)
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
        return thikr.title != null && thikr.title.isNotEmpty()
    }

    fun shouldShowTranslation(thikr: Thikr): Boolean {
        return repository.language != Language.ARABIC
                && thikr.textTranslation != null
                && thikr.textTranslation.isNotEmpty()
    }

    fun shouldShowFadl(thikr: Thikr): Boolean {
        return thikr.fadl != null && thikr.fadl.isNotEmpty()
    }

    fun shouldShowReference(thikr: Thikr): Boolean {
        return thikr.reference != null && thikr.reference.isNotEmpty()
    }

    fun shouldShowRepetition(thikr: Thikr): Boolean {
        return thikr.repetition != "1"
    }

}