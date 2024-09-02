package bassamalim.hidaya.features.prayers.prayersMain.ui

import android.os.Build
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.prayers.prayerSettings.ui.PrayerSettings
import bassamalim.hidaya.features.prayers.prayersMain.domain.PrayersDomain
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PrayersViewModel @Inject constructor(
    private val domain: PrayersDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private var dateOffset = mutableIntStateOf(0)
    private val viewedDate = Calendar.getInstance()
    private val prayerNames = domain.getPrayerNames()

    private val _uiState = MutableStateFlow(PrayersUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.location,
        flowOf(dateOffset),
        domain.getPrayerSettings()
    ) { state, location, dateOffset, prayerSettings ->
        val prayerTimeMap = location?.let {
            domain.getTimes(
                location = location,
                dateOffset = dateOffset.intValue
            )
        }

        state.copy(
            prayersData = prayerNames.map {
                PrayerCardData(
                    pid = it.key,
                    text = "${it.value} ${prayerTimeMap?.get(it.key) ?: ""}",
                    notificationType = prayerSettings[it.key]!!.notificationType,
                    isTimeOffsetSpecified = prayerSettings[it.key]!!.timeOffset != 0,
                    timeOffset = formatOffset(prayerSettings[it.key]!!.timeOffset),
                    isReminderOffsetSpecified = prayerSettings[it.key]!!.reminderOffset != 0,
                    reminderOffset = formatOffset(prayerSettings[it.key]!!.reminderOffset),
                )
            },
            tutorialDialogShown = domain.getShouldShowTutorial(),
            isLocationAvailable = location != null,
            locationName =
                if (location != null) getLocationName()
                else "",
            shouldShowLocationFailedToast = location == null,
            dateText = getDateText(dateOffset.intValue),
    )}.stateIn(
        initialValue = PrayersUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
        }
    }

    fun onLocatorClick() {
        navigator.navigate(
            Screen.Locator(isInitial = false.toString())
        )
    }

    fun onPrayerCardClick(pid: PID) {
        if (_uiState.value.isLocationAvailable) {
            navigator.navigateForResult(
                Screen.PrayerSettings(pid = pid.name)
            ) { result ->
                if (result != null) {
                    onSettingsDialogSave(
                        pid = pid,
                        settings =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                result.getParcelable(
                                    "prayer_settings",
                                    PrayerSettings::class.java
                                )!!
                            else
                                result.getParcelable("prayer_settings")!!
                    )
                }
            }
        }
    }

    private fun onSettingsDialogSave(pid: PID, settings: PrayerSettings) {
        viewModelScope.launch {
            domain.updatePrayerSettings(
                pid = pid,
                prayerSettings = settings
            )

            domain.updatePrayerTimeAlarms(pid)
        }
    }

    fun onReminderCardClick(pid: PID) {
        if (_uiState.value.isLocationAvailable) {
            navigator.navigateForResult(
                destination = Screen.PrayerReminder(
                    pid = pid.name
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
                    reminderOffset = formatOffset(offset)
                )
            }.toList()
        )}

        viewModelScope.launch {
            domain.updatePrayerSettings(
                pid = pid,
                prayerSettings = PrayerSettings(
                    notificationType = _uiState.value.prayersData[prayerNum].notificationType,
                    timeOffset = _uiState.value.prayersData[prayerNum].timeOffset.toInt(),
                    reminderOffset = offset
                )
            )

            domain.updatePrayerTimeAlarms(pid)
        }

    }

    fun onPreviousDayClick() {
        dateOffset.intValue--
        viewedDate.add(Calendar.DATE, -1)
    }

    fun onDateClick() {
        dateOffset.intValue = 0
        viewedDate.time = Calendar.getInstance().time
    }

    fun onNextDayClick() {
        dateOffset.intValue++
        viewedDate.add(Calendar.DATE, 1)
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) {
            viewModelScope.launch {
                domain.setDoNotShowAgain()
            }
        }
    }

    private suspend fun getLocationName(): String {
        val location = domain.location.first()!!

        val countryName = domain.getCountryName(
            countryId = location.ids.countryId,
            language = language
        )
        val cityName = domain.getCityName(cityId = location.ids.cityId, language = language)
        return "$countryName, $cityName"
    }

    private fun getDateText(dateOffset: Int): String {
        return if (dateOffset == 0) ""
        else {
            val hijri = UmmalquraCalendar()
            hijri.time = viewedDate.time

            val year = translateNums(
                numeralsLanguage = numeralsLanguage,
                string = hijri[Calendar.YEAR].toString()
            )
            val month = domain.getHijriMonths()[hijri[Calendar.MONTH]]
            val day = translateNums(
                numeralsLanguage = numeralsLanguage,
                string = hijri[Calendar.DATE].toString()
            )

            "$day $month $year"
        }
    }

    private fun formatOffset(offset : Int): String {
        return if (offset < 0) translateNums(numeralsLanguage, offset.toString())
        else translateNums(numeralsLanguage, "+$offset")
    }

}