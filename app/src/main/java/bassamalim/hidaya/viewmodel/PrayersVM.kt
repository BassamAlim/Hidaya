package bassamalim.hidaya.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.Screen
import bassamalim.hidaya.activities.MainActivity
import bassamalim.hidaya.enum.LocationType
import bassamalim.hidaya.enum.NotificationType
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.repository.PrayersRepo
import bassamalim.hidaya.state.PrayersState
import bassamalim.hidaya.utils.LangUtils
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
    app: Application,
    private val repository: PrayersRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {

    private val isLocated = savedStateHandle.get<Boolean>("is_located") ?: false
    private val coordinates =
        savedStateHandle.get<FloatArray>("coordinates") ?: floatArrayOf(0f, 0f)

    private val context = app.applicationContext
    private val location = Location("").apply {
        latitude = coordinates[0].toDouble()
        longitude = coordinates[1].toDouble()
    }
    private val prayTimes = PrayTimes(repository.pref)
    private val prayerNames = repository.getPrayerNames()
    private val calendar = Calendar.getInstance()

    private val _uiState = MutableStateFlow(PrayersState(
        locationName = getLocationName(),
        prayerTexts = appendNames(getTimes()),
        notificationTypeIconIDs = getNotificationTypeIconID(getNotificationTypes()),
        timeOffsetTexts = formatTimeOffsets(getTimeOffsets()),
        dateText = getDateText()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        if (isLocated) goToToday()
    }

    fun goToToday() {
        getTimes()
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
            location.latitude, location.longitude, utcOffset.toDouble(), calendar
        )
    }

    private fun getLocationName(): String {
        if (!isLocated) return ""

        var countryId = repository.getCountryID()
        var cityId = repository.getCityID()

        if (repository.getLocationType() == LocationType.Auto || countryId == -1 || cityId == -1) {
            val closest = repository.getClosest(location.latitude, location.longitude)
            countryId = closest.countryId
            cityId = closest.id
        }

        val countryName = repository.getCountryName(countryId)
        val cityName = repository.getCityName(cityId)

        return "$countryName, $cityName"
    }

    fun previousDay() {
        if (isLocated) {
            _uiState.update { it.copy(
                dateOffset = it.dateOffset - 1,
                dateText = getDateText()
            )}
        }
    }

    fun nextDay() {
        if (isLocated) {
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

            val year = LangUtils.translateNums(
                repository.numeralsLanguage, hijri[Calendar.YEAR].toString(), false
            )
            val month = repository.getHijriMonths()[hijri[Calendar.MONTH]]
            val day = LangUtils.translateNums(
                repository.numeralsLanguage, hijri[Calendar.DATE].toString(), false
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
                    LangUtils.translateNums(repository.numeralsLanguage, "+$offset")
                else if (offset < 0)
                    LangUtils.translateNums(repository.numeralsLanguage, offset.toString())
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
        if (isLocated) {
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

        Alarms(context, _uiState.value.settingsDialogPID)
    }

    fun onTimeOffsetChange(timeOffset: Int) {
        _uiState.update { it.copy(
            timeOffsetTexts = formatTimeOffsets(repository.getTimeOffsets())
        )}
        repository.setTimeOffset(_uiState.value.settingsDialogPID, timeOffset)

        Alarms(context, _uiState.value.settingsDialogPID)
    }

    fun onSettingsDialogDismiss() {
        _uiState.update { it.copy(
            isSettingsDialogShown = false
        )}

        Keeper(repository.pref, MainActivity.location!!)
        Alarms(context, _uiState.value.settingsDialogPID)
    }

}