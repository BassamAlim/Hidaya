package bassamalim.hidaya.features.prayers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.helpers.PrayTimes
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.PTUtils
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
    private val repo: PrayersRepo
): AndroidViewModel(app) {

    val location = repo.getLocation()
    private var prayTimes = PrayTimes(repo.sp)
    private val prayerNames = repo.getPrayerNames()
    private val calendar = Calendar.getInstance()
    private var dateOffset = 0

    private val _uiState = MutableStateFlow(PrayersState(
        notificationTypes = repo.getNotificationTypes(),
        timeOffsets = repo.getTimeOffsets(),
        tutorialDialogShown = repo.getShowTutorial(),
        locationName =
            if (location != null) getLocName()
            else repo.getClkToLocate()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        prayTimes = PrayTimes(repo.sp)  // to update in case of method changes

        goToToday()
    }

    private fun updateState(dateOffset: Int = 0) {
        this.dateOffset = dateOffset

        if (location != null) {
            _uiState.update { it.copy(
                prayerTexts = appendNames(getTimes()),
                dateText = getDateText()
            )}
        }
    }

    fun goToToday() {
        updateState(0)
    }

    /**
     * Gets the prayer times for the current day and the next day, and sets the text of the
     * prayer time screens to the prayer times
     */
    private fun getTimes(): List<String> {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + dateOffset

        val utcOffset = PTUtils.getUTCOffset(repo.sp, repo.db)

        return prayTimes.getStrPrayerTimes(
            location!!.latitude, location.longitude, utcOffset.toDouble(), calendar
        )
    }

    private fun getLocName(): String {
        var countryId = repo.getCountryID()
        var cityId = repo.getCityID()

        if (repo.getLocationType() == LocationType.Auto || countryId == -1 || cityId == -1) {
            val closest = repo.getClosest(location!!.latitude, location.longitude)
            countryId = closest.countryId
            cityId = closest.id
        }

        val countryName = repo.getCountryName(countryId)
        val cityName = repo.getCityName(cityId)

        return "$countryName, $cityName"
    }

    private fun getDateText(): String {
        return if (dateOffset == 0) repo.getDayStr()
        else {
            val hijri = UmmalquraCalendar()
            hijri.time = calendar.time

            val year = translateNums(
                repo.numeralsLanguage, hijri[Calendar.YEAR].toString()
            )
            val month = repo.getHijriMonths()[hijri[Calendar.MONTH]]
            val day = translateNums(
                repo.numeralsLanguage, hijri[Calendar.DATE].toString()
            )

            "$day $month $year"
        }
    }

    private fun appendNames(times: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in times.indices) result.add("${prayerNames[i]}: ${times[i]}")
        return result
    }

    fun formatTimeOffset(offset : Int): String {
        return if (offset > 0)
            translateNums(repo.numeralsLanguage, "+$offset")
        else if (offset < 0)
            translateNums(repo.numeralsLanguage, offset.toString())
        else ""
    }

    fun getNotificationTypeIconID(type: NotificationType): Int {
        return when (type) {
            NotificationType.Athan -> R.drawable.ic_speaker
            NotificationType.Notification -> R.drawable.ic_sound
            NotificationType.Silent -> R.drawable.ic_silent
            NotificationType.None -> R.drawable.ic_block
        }
    }

    private fun updatePrayerTimeAlarms() {
        Alarms(app, _uiState.value.settingsDialogPID)
    }

    fun onLocatorClick(navController: NavController) {
        navController.navigate(
            Screen.Locator(
                "normal"
            ).route
        )
    }

    fun onPrayerClick(pid: PID) {
        if (location != null) {
            _uiState.update { it.copy(
                settingsDialogShown = true,
                settingsDialogPID = pid
            )}
        }
    }

    fun onPreviousDayClk() {
        updateState(dateOffset - 1)
    }

    fun onNextDayClk() {
        updateState(dateOffset + 1)
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) repo.setDoNotShowAgain()
    }

    fun onNotificationTypeChange(notificationType: NotificationType) {
        _uiState.update { it.copy(
            notificationTypes = _uiState.value.notificationTypes.toMutableList().apply {
                this[_uiState.value.settingsDialogPID.ordinal] = notificationType
            }
        )}

        repo.setNotificationType(_uiState.value.settingsDialogPID, notificationType)

        updatePrayerTimeAlarms()
    }

    fun onTimeOffsetChange(timeOffset: Int) {
        _uiState.update { it.copy(
            timeOffsets = _uiState.value.timeOffsets.toMutableList().apply {
                this[_uiState.value.settingsDialogPID.ordinal] = timeOffset
            }
        )}

        repo.setTimeOffset(_uiState.value.settingsDialogPID, timeOffset)

        updatePrayerTimeAlarms()
    }

    fun onSettingsDialogDismiss() {
        _uiState.update { it.copy(
            settingsDialogShown = false
        )}

        updatePrayerTimeAlarms()
    }

}