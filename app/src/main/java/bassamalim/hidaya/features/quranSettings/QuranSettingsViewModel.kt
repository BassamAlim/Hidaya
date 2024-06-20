package bassamalim.hidaya.features.quranSettings

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.QuranViewType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranSettingsViewModel @Inject constructor(
    private val repo: QuranSettingsRepository
): ViewModel() {

    val sp = repo.sp
    val reciterNames = repo.getReciterNames()
    val reciterIds = Array(reciterNames.size) { idx -> idx.toString() }
    var viewType = repo.getViewType()

    fun onViewTypeCh(type: Int) {
        viewType = QuranViewType.entries.toTypedArray()[type]

        repo.setViewType(viewType)
    }

    fun onDone(mainOnDone: () -> Unit) {
        mainOnDone()
    }

}