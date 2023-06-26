package bassamalim.hidaya.features.prayerSetting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PrayerSettingVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: PrayerSettingsRepo,
    private val gson: Gson
): ViewModel() {

    private val pid = PID.valueOf(savedStateHandle.get<String>("pid") ?: "")

    private val _uiState = MutableStateFlow(PrayerSettingState(
        pid = pid,
        notificationType = repo.getNotificationType(pid),
        timeOffset = repo.getTimeOffset(pid),
        reminderOffset = repo.getReminderOffset(pid)
    ))
    val uiState = _uiState.asStateFlow()

    fun onNotificationTypeChange(notificationType: NotificationType) {
        _uiState.update { it.copy(
            notificationType = notificationType
        )}
    }

    fun onTimeOffsetChange(timeOffset: Int) {
        _uiState.update { it.copy(
            timeOffset = timeOffset
        )}
    }

    fun onReminderOffsetChange(reminderOffset: Int) {
        _uiState.update { it.copy(
            reminderOffset = reminderOffset
        )}
    }

    fun onDismiss(nc: NavController) {
        val prayerSettings = PrayerSettings(
            pid = pid,
            notificationType = uiState.value.notificationType,
            timeOffset = uiState.value.timeOffset,
            reminderOffset = uiState.value.reminderOffset
        )
        val prayerSettingsJson = gson.toJson(prayerSettings)

        nc.previousBackStackEntry
            ?.savedStateHandle
            ?.set("prayer_settings", prayerSettingsJson)

        nc.popBackStack()
    }

}