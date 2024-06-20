package bassamalim.hidaya.features.prayerReminder

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.nav.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PrayerReminderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: PrayerReminderRepository,
    private val navigator: Navigator
): ViewModel() {

    private val pid = PID.valueOf(savedStateHandle.get<String>("pid") ?: "")

    private val _uiState = MutableStateFlow(PrayerReminderState(
        pid = pid,
        prayerName = repo.getPrayerName(pid),
        offset = repo.getOffset(pid)
    ))
    val uiState = _uiState.asStateFlow()

    val offsetMin = 30f

    fun onOffsetChange(offset: Int) {
        _uiState.update { it.copy(
            offset = offset
        )}
    }

    fun onSave() {
        navigator.navigateBackWithResult(
            data = Bundle().apply {
                putInt("offset", _uiState.value.offset)
            }
        )
    }

    fun onDismiss() {
        navigator.navigateBackWithResult(
            data = null
        )
    }

}