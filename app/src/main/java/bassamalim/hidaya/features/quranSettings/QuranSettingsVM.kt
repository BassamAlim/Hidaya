package bassamalim.hidaya.features.quranSettings

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.QuranViewTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranSettingsVM @Inject constructor(
    private val repo: QuranSettingsRepo
): ViewModel() {

    val sp = repo.sp
    val reciterNames = repo.getReciterNames()
    val reciterIds = Array(reciterNames.size) { idx -> idx.toString() }
    var viewType = repo.getViewType()

    fun onViewTypeCh(type: Int) {
        viewType = QuranViewTypes.entries.toTypedArray()[type]

        repo.setViewType(viewType)
    }

    fun onDone(mainOnDone: () -> Unit) {
        mainOnDone()
    }

}