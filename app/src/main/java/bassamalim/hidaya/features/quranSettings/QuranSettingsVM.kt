package bassamalim.hidaya.features.quranSettings

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.QViewType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranSettingsVM @Inject constructor(
    private val repo: QuranSettingsRepo
): ViewModel() {

    val sp = repo.sp
    val reciterNames = repo.getReciterNames()
    val reciterIds = listOf(reciterNames.indices)
    var viewType = repo.getViewType()

    fun onViewTypeCh(type: Int) {
        viewType = QViewType.values()[type]

        repo.setViewType(viewType)
    }

    fun onDone(mainOnDone: () -> Unit) {
        mainOnDone()
    }

}