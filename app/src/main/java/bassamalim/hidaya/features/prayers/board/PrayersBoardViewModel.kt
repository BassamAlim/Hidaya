package bassamalim.hidaya.features.prayers.board

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.OsUtils
import bassamalim.hidaya.features.prayers.notificationSettings.PrayerNotificationSettings
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.SortedMap
import javax.inject.Inject

@HiltViewModel
class PrayersBoardViewModel @Inject constructor(
    private val app: Application,
    private val domain: PrayersBoardDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private val location = domain.getLocation()
    private val prayerSettings = domain.getPrayerSettings()
    private val prayerTimesCalculatorSettings = domain.getPrayerTimesCalculatorSettings()
    private val currentDate = Calendar.getInstance()
    private val viewedDate = Calendar.getInstance()
    private val prayerNames = domain.getPrayerNames()

    private val _uiState = MutableStateFlow(PrayersBoardUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        location,
        prayerSettings,
        prayerTimesCalculatorSettings
    ) { state, location, prayerSettings, prayerTimesCalculatorSettings ->
        if (location != null) {
            val prayerTimeMap = domain.getTimes(
                location = location,
                date = viewedDate,
                prayerTimesCalculatorSettings = prayerTimesCalculatorSettings
            )
            state.copy(
                locationAvailable = true,
                prayersData = getPrayersData(prayerTimeMap, prayerSettings),
                locationName = getLocationName(location)
            )
        }
        else state.copy(
            locationAvailable = false,
            locationName = ""
        )
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = PrayersBoardUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                loading = false,
                dateText = getDateText(viewedDate)
            )}
        }
    }

    fun onLocatorClick() {
        navigator.navigate(Screen.Locator(isInitial = false.toString()))
    }

    fun onTimeCalculationSettingsClick() {
        navigator.navigate(Screen.PrayerTimeCalculationSettings)
    }

    fun onPrayerCardClick(prayer: Prayer) {
        navigator.navigate(Screen.PrayerSettings(prayerName = prayer.name))
    }

    fun onExtraReminderCardClick(prayer: Prayer) {
        navigator.navigate(Screen.PrayerExtraReminderSettings(prayerName = prayer.name))
    }

    fun onPreviousDayClick() {
        val newDate = (viewedDate.clone() as Calendar).apply { add(Calendar.DATE, -1) }
        updateDate(newDate)
    }

    fun onDateClick() {
        val newDate = (viewedDate.clone() as Calendar).apply { time = currentDate.time }
        updateDate(newDate)
    }

    fun onNextDayClick() {
        val newDate = (viewedDate.clone() as Calendar).apply { add(Calendar.DATE, 1) }
        updateDate(newDate)
    }

    fun onReportHelpClick() {
        viewModelScope.launch {
            val location = location.first()
            val settings = prayerTimesCalculatorSettings.first()
            val methodEntries =
                app.resources.getStringArray(R.array.prayer_times_calc_method_entries)
            val methodName = methodEntries.getOrNull(settings.calculationMethod.ordinal).orEmpty()
            val computed = location?.let {
                domain.getTimes(
                    location = it,
                    date = currentDate,
                    prayerTimesCalculatorSettings = settings
                )
            } ?: sortedMapOf()

            _uiState.update { it.copy(
                reportDialogShown = true,
                reportStep = ReportStep.CHECKS,
                reportCurrentMethodName = methodName,
                reportIsAutoLocation = location?.type == LocationType.AUTO,
                reportPrayerNames = prayerNames,
                reportComputedTimes = computed,
                reportWrongPrayers = emptySet(),
                reportCorrectTimes = emptyMap(),
                reportNotes = "",
                reportSubmitting = false,
                reportSubmitted = false,
                reportError = null
            )}
        }
    }

    fun onReportDismiss() {
        _uiState.update { it.copy(reportDialogShown = false) }
    }

    fun onReportNext() {
        _uiState.update {
            val next = when (it.reportStep) {
                ReportStep.CHECKS -> ReportStep.FORM
                else -> it.reportStep
            }
            it.copy(reportStep = next)
        }
    }

    fun onReportBack() {
        _uiState.update {
            val prev = when (it.reportStep) {
                ReportStep.FORM -> ReportStep.CHECKS
                else -> it.reportStep
            }
            it.copy(reportStep = prev)
        }
    }

    fun onReportTogglePrayer(prayer: Prayer) {
        _uiState.update {
            val newSet =
                if (prayer in it.reportWrongPrayers) it.reportWrongPrayers - prayer
                else it.reportWrongPrayers + prayer
            it.copy(reportWrongPrayers = newSet)
        }
    }

    fun onCorrectTimePickerOpen(prayer: Prayer) {
        _uiState.update { it.copy(reportTimePickerTarget = prayer) }
    }

    fun onCorrectTimePickerDismiss() {
        _uiState.update { it.copy(reportTimePickerTarget = null) }
    }

    fun onCorrectTimePickerConfirm(hour: Int, minute: Int) {
        _uiState.update {
            val prayer = it.reportTimePickerTarget ?: return@update it
            val value = "%02d:%02d".format(hour, minute)
            it.copy(
                reportCorrectTimes = it.reportCorrectTimes + (prayer to value),
                reportTimePickerTarget = null
            )
        }
    }

    fun onReportNotesChange(value: String) {
        _uiState.update { it.copy(reportNotes = value) }
    }

    fun onReportSubmit() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                reportStep = ReportStep.RESULT,
                reportSubmitting = true,
                reportError = null
            )}

            val location = location.first()
            val settings = prayerTimesCalculatorSettings.first()
            val state = _uiState.value

            val report = mutableMapOf<String, Any?>(
                "device_id" to OsUtils.getDeviceId(app),
                "app_version" to getAppVersionName(),
                "language" to language.name,
                "location_type" to location?.type?.name,
                "latitude" to location?.coordinates?.latitude,
                "longitude" to location?.coordinates?.longitude,
                "country_id" to location?.ids?.countryId,
                "city_id" to location?.ids?.cityId,
                "location_name" to state.locationName,
                "calculation_method" to settings.calculationMethod.name,
                "juristic_method" to settings.juristicMethod.name,
                "high_latitudes_adjustment" to settings.highLatitudesAdjustmentMethod.name,
                "computed_times" to state.reportComputedTimes.mapKeys { it.key.name },
                "wrong_prayers" to state.reportWrongPrayers.map { prayer ->
                    mapOf(
                        "prayer" to prayer.name,
                        "computed" to state.reportComputedTimes[prayer],
                        "correct" to state.reportCorrectTimes[prayer].orEmpty()
                    )
                },
                "notes" to state.reportNotes
            )

            val success = domain.submitReport(report)

            _uiState.update { it.copy(
                reportSubmitting = false,
                reportSubmitted = success,
                reportError =
                    if (success) null
                    else app.getString(R.string.report_submit_failed)
            )}
        }
    }

    private fun getAppVersionName(): String =
        try {
            app.packageManager.getPackageInfo(app.packageName, 0).versionName.orEmpty()
        } catch (_: Exception) {
            ""
        }

    private fun updateDate(newDate: Calendar) {
        viewModelScope.launch {
            val location = location.first() ?: return@launch
            val prayerSettings = prayerSettings.first()
            val prayerTimesCalculatorSettings = prayerTimesCalculatorSettings.first()

            val prayerTimeMap = domain.getTimes(
                location = location,
                date = viewedDate,
                prayerTimesCalculatorSettings = prayerTimesCalculatorSettings
            )
            _uiState.update { it.copy(
                dateText = getDateText(newDate),
                prayersData = getPrayersData(prayerTimeMap, prayerSettings),
                noDateOffset = newDate == currentDate
            )}

            viewedDate.time = newDate.time
        }
    }

    private fun getPrayersData(
        prayerTimeMap: SortedMap<Prayer, String>,
        prayerSettings: Map<Prayer, PrayerNotificationSettings>
    ) = sortedMapOf<Prayer, PrayerCardData>().apply {
        prayerNames.forEach { (prayer, name) ->
            this[prayer] = PrayerCardData(
                text = "$name ${prayerTimeMap[prayer] ?: ""}",
                notificationType = prayerSettings[prayer]!!.notificationType,
                isExtraReminderOffsetSpecified = prayerSettings[prayer]!!.reminderOffset != 0,
                extraReminderOffset = formatOffset(prayerSettings[prayer]!!.reminderOffset)
            )
        }
    }

    private suspend fun getLocationName(location: Location): String {
        val countryName = domain.getCountryName(
            countryId = location.ids.countryId,
            language = language
        )
        val cityName = domain.getCityName(cityId = location.ids.cityId, language = language)
        return "$countryName, $cityName"
    }

    private fun getDateText(newDate: Calendar): String {
        return if (newDate == currentDate) ""
        else {
            val hijri = UmmalquraCalendar()
            hijri.time = newDate.time

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
