package bassamalim.hidaya.features.athkarViewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Thikr
import bassamalim.hidaya.features.navArgs
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

    private val navArgs = savedStateHandle.navArgs<AthkarViewerNavArgs>()

    private val language = repo.getLanguage()

    private val _uiState = MutableStateFlow(AthkarViewerState(
        title = repo.getTitle(navArgs.thikrId),
        textSize = repo.getTextSize(),
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<Thikr> {
        val thikrs = repo.getThikrs(navArgs.thikrId)

        val items = ArrayList<Thikr>()
        for (i in thikrs.indices) {
            val t = thikrs[i]

            if (language == Language.ENGLISH &&
                (t.getTextEn() == null || t.getTextEn()!!.isEmpty()))
                continue

            if (language == Language.ENGLISH)
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