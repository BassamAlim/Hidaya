package bassamalim.hidaya.features.prayerSetting

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle

@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun PrayerSettingDialog(
    vm: PrayerSettingVM = hiltViewModel()
) {
//    val st by vm.uiState.collectAsStateWithLifecycle()


}