package bassamalim.hidaya.features.prayerSetting

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.features.prayers.PrayersState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PrayerSettingVM @Inject constructor(
    private val repo: PrayerSettingRepo
): ViewModel() {

//    private val _uiState = MutableStateFlow(PrayerSettingState(
//    ))
//    val uiState = _uiState.asStateFlow()

}