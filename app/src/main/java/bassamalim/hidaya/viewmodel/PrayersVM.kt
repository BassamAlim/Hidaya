package bassamalim.hidaya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enum.LocationType
import bassamalim.hidaya.enum.NotificationType
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.repository.PrayersRepo
import bassamalim.hidaya.state.PrayersState
import bassamalim.hidaya.utils.LangUtils.translateNums
import bassamalim.hidaya.utils.PTUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PrayersVM @Inject constructor(
    private val app: Application,
    private val repository: PrayersRepo
): AndroidViewModel(app) {

    private val location = repository.getLocation()
    private val prayTimes = PrayTimes(repository.pref)
    private val prayerNames = repository.getPrayerNames()
    private val calendar = Calendar.getInstance()

    private val _uiState = MutableStateFlow(PrayersState())
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        goToToday()
    }

    private fun updateState(dateOffset: Int = 0) {
        if (location != null) {
            _uiState.update { it.copy(
                locationName = getLocationName(),
                prayerTexts = appendNames(getTimes()),
                notificationTypeIconIDs = getNotificationTypeIconID(getNotificationTypes()),
                timeOffsetTexts = formatTimeOffsets(getTimeOffsets()),
                dateOffset = dateOffset,
                dateText = getDateText()
            )}
        }
    }

    fun goToToday() {
        updateState()
    }

    /**
     * Gets the prayer times for the current day and the next day, and sets the text of the
     * prayer time screens to the prayer times
     */
    private fun getTimes(): List<String> {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + _uiState.value.dateOffset

        val utcOffset = PTUtils.getUTCOffset(repository.pref, repository.db)

        return prayTimes.getStrPrayerTimes(
            location!!.latitude, location.longitude, utcOffset.toDouble(), calendar
        )
    }

    private fun getLocationName(): String {
        var countryId = repository.getCountryID()
        var cityId = repository.getCityID()

        if (repository.getLocationType() == LocationType.Auto || countryId == -1 || cityId == -1) {
            val closest = repository.getClosest(location!!.latitude, location.longitude)
            countryId = closest.countryId
            cityId = closest.id
        }

        val countryName = repository.getCountryName(countryId)
        val cityName = repository.getCityName(cityId)

        return "$countryName, $cityName"
    }

    fun previousDay() {
        if (location != null) {
            _uiState.update { it.copy(
                dateOffset = it.dateOffset - 1,
                dateText = getDateText()
            )}
        }
    }

    fun nextDay() {
        if (location != null) {
            _uiState.update { it.copy(
                dateOffset = it.dateOffset + 1,
                dateText = getDateText()
            )}
        }
    }

    private fun getDateText(): String {
        return if (_uiState.value.dateOffset == 0) repository.getTodayText()
        else {
            val hijri = UmmalquraCalendar()
            hijri.time = calendar.time

            val year = translateNums(
                repository.numeralsLanguage, hijri[Calendar.YEAR].toString()
            )
            val month = repository.getHijriMonths()[hijri[Calendar.MONTH]]
            val day = translateNums(
                repository.numeralsLanguage, hijri[Calendar.DATE].toString()
            )

            "$day $month $year"
        }
    }

    private fun appendNames(times: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in times.indices) result.add("${prayerNames[i]}: ${times[i]}")
        return result
    }

    private fun formatTimeOffsets(offsets : List<Int>): List<String> {
        val result = mutableListOf<String>()

        offsets.forEach { offset ->
            result.add(
                if (offset > 0)
                    translateNums(repository.numeralsLanguage, "+$offset")
                else if (offset < 0)
                    translateNums(repository.numeralsLanguage, offset.toString())
                else ""
            )
        }

        return result
    }

    private fun getNotificationTypeIconID(types: List<NotificationType>): List<Int> {
        val result = mutableListOf<Int>()
        types.forEach { type ->
            result.add(
                when (type) {
                    NotificationType.Athan -> R.drawable.ic_speaker
                    NotificationType.Notification -> R.drawable.ic_sound
                    NotificationType.Silent -> R.drawable.ic_silent
                    NotificationType.None -> R.drawable.ic_block
                }
            )
        }
        return result
    }

    fun getNotificationTypes() = repository.getNotificationTypes()

    fun getTimeOffsets() = repository.getTimeOffsets()

    fun onLocatorClick(navController: NavController) {
        navController.navigate(
            Screen.Locator.withArgs(
                "normal"
            )
        )
    }

    fun onPrayerClick(pid: PID) {
        if (location != null) {
            _uiState.update { it.copy(
                isSettingsDialogShown = true,
                settingsDialogPID = pid
            )}
        }
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            isTutorialDialogShown = false
        )}

        if (doNotShowAgain) repository.setDoNotShowAgain()
    }

    fun onNotificationTypeChange(notificationType: NotificationType) {
        _uiState.update { it.copy(
            notificationTypeIconIDs = getNotificationTypeIconID(repository.getNotificationTypes())
        )}
        repository.setNotificationType(_uiState.value.settingsDialogPID, notificationType)

        updatePrayerTimeAlarms()
    }

    fun onTimeOffsetChange(timeOffset: Int) {
        _uiState.update { it.copy(
            timeOffsetTexts = formatTimeOffsets(repository.getTimeOffsets())
        )}
        repository.setTimeOffset(_uiState.value.settingsDialogPID, timeOffset)

        updatePrayerTimeAlarms()
    }

    fun onSettingsDialogDismiss() {
        _uiState.update { it.copy(
            isSettingsDialogShown = false
        )}

        updatePrayerTimeAlarms()
    }

    private fun updatePrayerTimeAlarms() {
        Alarms(app.applicationContext, _uiState.value.settingsDialogPID)
    }

}