package bassamalim.hidaya.features.prayers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.helpers.PrayTimes
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.features.destinations.LocatorUIDestination
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PrayersVM @Inject constructor(
    private val app: Application,
    private val repo: PrayersRepo
): AndroidViewModel(app) {

    val location = repo.getLocation()
    private var prayTimes = PrayTimes(repo.sp)
    private val calendar = Calendar.getInstance()
    private var dateOffset = 0

    private val _uiState = MutableStateFlow(PrayersState(
        prayersData = repo.getPrayersData(),
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

    fun goToToday() {
        updateState(0)
    }

    private fun updateState(dateOffset: Int = 0) {
        this.dateOffset = dateOffset

        if (location != null) {
            val times = getTimes()
            val prayersData = _uiState.value.prayersData.mapIndexed { idx, data ->
                data.copy(time = times[idx])
            }

            _uiState.update { it.copy(
                prayersData = prayersData,
                dateText = getDateText()
            )}
        }
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

    fun onLocatorClick(navigator: DestinationsNavigator) {
        navigator.navigate(
            LocatorUIDestination(
                type = "normal"
            )
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
        val prayerNum = _uiState.value.settingsDialogPID.ordinal
        _uiState.update { it.copy(
            prayersData = it.prayersData.toMutableList().apply {
                this[prayerNum] = it.prayersData[prayerNum].copy(
                    notificationType = notificationType
                )
            }.toList()
        )}

        repo.setNotificationType(_uiState.value.settingsDialogPID, notificationType)

        updatePrayerTimeAlarms()
    }

    fun onTimeOffsetChange(timeOffset: Int) {
        val prayerNum = _uiState.value.settingsDialogPID.ordinal
        _uiState.update { it.copy(
            prayersData = it.prayersData.toMutableList().apply {
                this[prayerNum] = it.prayersData[prayerNum].copy(
                    timeOffset = timeOffset
                )
            }.toList()
        )}

        repo.setTimeOffset(_uiState.value.settingsDialogPID, timeOffset)

        updatePrayerTimeAlarms()
    }

    fun onReminderOffsetChange(reminderOffset: Int) {
        val prayerNum = _uiState.value.settingsDialogPID.ordinal
        _uiState.update { it.copy(
            prayersData = it.prayersData.toMutableList().apply {
                this[prayerNum] = it.prayersData[prayerNum].copy(
                    reminderOffset = reminderOffset
                )
            }.toList()
        )}

        repo.setReminderOffset(_uiState.value.settingsDialogPID, reminderOffset)

        updatePrayerTimeAlarms()
    }

    fun onSettingsDialogDismiss() {
        _uiState.update { it.copy(
            settingsDialogShown = false
        )}

        updatePrayerTimeAlarms()
    }

}