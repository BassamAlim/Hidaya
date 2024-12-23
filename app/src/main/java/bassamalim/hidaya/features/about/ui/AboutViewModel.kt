package bassamalim.hidaya.features.about.ui

import android.app.Activity
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.features.about.domain.AboutDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val domain: AboutDomain
): ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState(
        sources = domain.getSources()
    ))
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getLastUpdate()
    ) { state, lastUpdate -> state.copy(
        lastDailyUpdate = formatLastUpdate(lastUpdate)
    )}.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = AboutUiState()
    )

    fun onRebuildDatabaseClick(
        activity: Activity,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        viewModelScope.launch {
            domain.rebuildDatabase(activity)
            snackbarHostState.showSnackbar(message)
        }
    }

    fun onResetTutorials(snackbarHostState: SnackbarHostState, message: String) {
        viewModelScope.launch {
            domain.resetTutorials()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun onTitleClick() {
        domain.handleTitleClicks(
            setDevModeEnabled = {
                _uiState.update { it.copy(
                    isDevModeEnabled = true
                )}
            }
        )
    }

    private fun formatLastUpdate(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis

        return "Last Daily Update: " +
                "${calendar[Calendar.YEAR]}/" +
                "${calendar[Calendar.MONTH] + 1}/" +
                "${calendar[Calendar.DATE]} " +
                "${calendar[Calendar.HOUR_OF_DAY]}:${calendar[Calendar.MINUTE]}"
    }

}