package bassamalim.hidaya.features.prayerSetting

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.nav.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PrayerSettingVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: PrayerSettingsRepo,
    private val navigator: Navigator
): ViewModel() {

    private val pid = PID.valueOf(savedStateHandle.get<String>("pid") ?: "")

    private val _uiState = MutableStateFlow(PrayerSettingState(
        pid = pid,
        prayerName = repo.getPrayerName(pid),
        notificationType = repo.getNotificationType(pid),
        timeOffset = repo.getTimeOffset(pid)
    ))
    val uiState = _uiState.asStateFlow()

    val offsetMin = 30f
    val notificationTypeOptions = listOf(
        Pair(R.string.athan_speaker, R.drawable.ic_speaker),
        Pair(R.string.enable_notification, R.drawable.ic_sound),
        Pair(R.string.silent_notification, R.drawable.ic_silent),
        Pair(R.string.disable_notification, R.drawable.ic_block)
    )

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

    fun onSave() {
        val prayerSettings = PrayerSettings(
            pid = pid,
            notificationType = uiState.value.notificationType,
            timeOffset = uiState.value.timeOffset
        )

        navigator.navigateBackWithResult(
            data = Bundle().apply {
                putParcelable("prayer_settings", prayerSettings)
            }
        )
    }

    fun onDismiss() {
        navigator.navigateBackWithResult(
            data = null
        )
    }

}