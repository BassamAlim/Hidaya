package bassamalim.hidaya.features.prayers

import android.app.Application
import android.location.Location
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.helpers.PrayTimes
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.features.prayerSetting.PrayerSettings
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PrayersVM @Inject constructor(
    private val app: Application,
    private val repo: PrayersRepo,
    private val navigator: Navigator
): AndroidViewModel(app) {

    var location = repo.getLocation()
    private lateinit var prayTimes: PrayTimes
    private val calendar = Calendar.getInstance()
    private var dateOffset = 0

    private val _uiState = MutableStateFlow(PrayersState(
        prayersData = repo.getPrayersData(),
        tutorialDialogShown = repo.getShowTutorial(),
        locationName =
            if (location != null) getLocationName()
            else repo.getClkToLocateStr()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        prayTimes = PrayTimes(repo.sp)  // to update in case of method changes

        updateState()
    }

    fun onLocatorClk() {
        navigator.navigateForResult(
            Screen.Locator(
                type = "normal"
            )
        ) { result ->
            if (result != null) {
                onLocationSet(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            result.getParcelable("location", Location::class.java)
                    else
                        result.getParcelable("location")
                )
            }
        }
    }

    private fun onLocationSet(location: Location?) {
        if (location != null) this.location = location

        _uiState.update { oldState -> oldState.copy(
            locationName = getLocationName()
        )}

        updateState()
    }

    fun onPrayerCardClk(pid: PID) {
        if (location != null) {
            navigator.navigateForResult(
                destination = Screen.PrayerSettings(
                    pid.name
                )
            ) { result ->
                if (result != null) {
                    onSettingsDialogSave(
                        pid = pid,
                        settings =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                result.getParcelable("prayer_settings", PrayerSettings::class.java)!!
                            else
                                result.getParcelable("prayer_settings")!!
                    )
                }
            }
        }
    }

    private fun onSettingsDialogSave(pid: PID, settings: PrayerSettings) {
        val prayerNum = pid.ordinal

        _uiState.update { oldState -> oldState.copy(
            prayersData = oldState.prayersData.toMutableList().apply {
                this[prayerNum] = oldState.prayersData[prayerNum].copy(
                    settings = oldState.prayersData[prayerNum].settings.copy(
                        notificationType = settings.notificationType,
                        timeOffset = settings.timeOffset
                    )
                )
            }.toList()
        )}

        repo.updatePrayerSettings(pid, _uiState.value.prayersData[prayerNum].settings)

        updatePrayerTimeAlarms(pid)
    }

    fun onReminderCardClk(pid: PID) {
        if (location != null) {
            navigator.navigateForResult(
                destination = Screen.PrayerReminder(
                    pid.name
                )
            ) { result ->
                if (result != null) {
                    onReminderDialogSave(
                        pid = pid,
                        offset = result.getInt("offset")
                    )
                }
            }
        }
    }

    private fun onReminderDialogSave(pid: PID, offset: Int) {
        val prayerNum = pid.ordinal

        _uiState.update { oldState -> oldState.copy(
            prayersData = oldState.prayersData.toMutableList().apply {
                this[prayerNum] = oldState.prayersData[prayerNum].copy(
                    settings = oldState.prayersData[prayerNum].settings.copy(
                        reminderOffset = offset
                    )
                )
            }.toList()
        )}

        repo.updatePrayerSettings(pid, _uiState.value.prayersData[prayerNum].settings)

        updatePrayerTimeAlarms(pid)
    }

    fun onPreviousDayClk() {
        updateState(dateOffset - 1)
    }

    fun onTodayClk() {
        updateState(0)
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

    private fun updatePrayerTimeAlarms(pid: PID) {
        Alarms(app, pid)
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
            location!!.latitude, location!!.longitude, utcOffset.toDouble(), calendar
        )
    }

    private fun getLocationName(): String {
        var countryId = repo.getCountryID()
        var cityId = repo.getCityID()

        if (repo.getLocationType() == LocationType.Auto || countryId == -1 || cityId == -1) {
            val closest = repo.getClosest(location!!.latitude, location!!.longitude)
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

    fun formatOffset(offset : Int): String {
        return if (offset < 0) translateNums(repo.numeralsLanguage, offset.toString())
        else translateNums(repo.numeralsLanguage, "+$offset")
    }

    fun getNotificationTypeIconID(type: NotificationType): Int {
        return when (type) {
            NotificationType.Athan -> R.drawable.ic_speaker
            NotificationType.Notification -> R.drawable.ic_sound
            NotificationType.Silent -> R.drawable.ic_silent
            NotificationType.None -> R.drawable.ic_block
        }
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

}