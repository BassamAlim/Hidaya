package bassamalim.hidaya.features.prayers.board.ui

import android.os.Build
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.prayers.board.domain.PrayersBoardDomain
import bassamalim.hidaya.features.prayers.settings.ui.PrayerSettings
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
class PrayersBoardViewModel @Inject constructor(
    private val domain: PrayersBoardDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private var dateOffset = mutableIntStateOf(0)
    private val viewedDate = Calendar.getInstance()
    private val prayerNames = domain.getPrayerNames()

    private val _uiState = MutableStateFlow(PrayersBoardUiState())
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
                it.key to PrayerCardData(
                    text = "${it.value} ${prayerTimeMap?.get(it.key) ?: ""}",
                    notificationType = prayerSettings[it.key]!!.notificationType,
                    isExtraReminderOffsetSpecified = prayerSettings[it.key]!!.reminderOffset != 0,
                    extraReminderOffset = formatOffset(prayerSettings[it.key]!!.reminderOffset),
                )
            }.toMap().toSortedMap(),
            tutorialDialogShown = domain.getShouldShowTutorial(),
            isLocationAvailable = location != null,
            locationName =
                if (location != null) getLocationName()
                else "",
            shouldShowLocationFailedToast = location == null,
            dateText = getDateText(dateOffset.intValue),
    )}.stateIn(
        initialValue = PrayersBoardUiState(),
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

    fun onPrayerCardClick(prayer: Prayer) {
        if (_uiState.value.isLocationAvailable) {
            navigator.navigateForResult(
                Screen.PrayerSettings(prayer = prayer.name)
            ) { result ->
                if (result != null) {
                    onSettingsDialogSave(
                        prayer = prayer,
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

    private fun onSettingsDialogSave(prayer: Prayer, settings: PrayerSettings) {
        viewModelScope.launch {
            domain.updatePrayerSettings(prayer = prayer, prayerSettings = settings)

            domain.updatePrayerTimeAlarms(prayer)
        }
    }

    fun onReminderCardClick(prayer: Prayer) {
        if (_uiState.value.isLocationAvailable) {
            navigator.navigateForResult(
                destination = Screen.PrayerReminderSettings(
                    prayer = prayer.name
                )
            ) { result ->
                if (result != null) {
                    onReminderDialogSave(
                        prayer = prayer,
                        offset = result.getInt("offset")
                    )
                }
            }
        }
    }

    private fun onReminderDialogSave(prayer: Prayer, offset: Int) {
        _uiState.update { oldState -> oldState.copy(
            prayersData = oldState.prayersData.apply {
                this[prayer] = oldState.prayersData[prayer]?.copy(
                    extraReminderOffset = formatOffset(offset)
                )
            }
        )}

        viewModelScope.launch {
            domain.updatePrayerSettings(
                prayer = prayer,
                prayerSettings = PrayerSettings(
                    notificationType = _uiState.value.prayersData[prayer]!!.notificationType,
                    reminderOffset = offset
                )
            )

            domain.updatePrayerTimeAlarms(prayer)
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
        return if (offset < 0) translateNums(
            string = offset.toString(),
            numeralsLanguage = numeralsLanguage
        )
        else translateNums(string = "+$offset", numeralsLanguage = numeralsLanguage)
    }

}