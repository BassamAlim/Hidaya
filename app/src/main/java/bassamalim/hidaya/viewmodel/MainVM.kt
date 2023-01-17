package bassamalim.hidaya.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
import bassamalim.hidaya.repository.MainRepo
import bassamalim.hidaya.state.MainState
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    app: Application,
    savedStateHandle: SavedStateHandle,
    private val repository: MainRepo
): AndroidViewModel(app) {

    private val isLocated = savedStateHandle.get<Boolean>("is_located") ?: false
    private val coordinates =
        savedStateHandle.get<FloatArray>("coordinates") ?: floatArrayOf(0f, 0f)

    private val _uiState = MutableStateFlow(MainState(
        hijriDate = getHijriDate(),
        gregorianDate = getGregorianDate()
    ))
    val uiState = _uiState.asStateFlow()

    private val context = app.applicationContext
    private val location = Location("")
    private var dateOffset = repository.getDateOffset()

    init {
        location.latitude = coordinates[0].toDouble()
        location.longitude = coordinates[1].toDouble()

        initFirebase()

        dailyUpdate()

        setAlarms()

        setupBootReceiver()

        updateDateEditor()
    }

    fun showDateEditor() {
        _uiState.update { it.copy(
            dateEditorShown = true
        )}
    }

    private fun initFirebase() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            // update at most every six hours
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun setAlarms() {
        if (isLocated) {
            Keeper(repository.pref, location)
            val times = PTUtils.getTimes(
                repository.pref,
                DBUtils.getDB(context),
                location
            )!!
            Alarms(context, times)
        }
        else {
            _uiState.update { it.copy(
                shouldShowLocationPermissionToast = true
            )}
        }
    }

    private fun dailyUpdate() {
        val intent = Intent(context, DailyUpdateReceiver::class.java)
        intent.action = "daily"
        context.sendBroadcast(intent)
    }

    private fun getHijriDate(): String {
        val hijri = UmmalquraCalendar()
        val hDayName = repository.getWeekDays()[hijri[Calendar.DAY_OF_WEEK] - 1]

        val millisInDay = 1000 * 60 * 60 * 24
        hijri.timeInMillis = hijri.timeInMillis + dateOffset * millisInDay

        val hMonth = repository.getHijriMonths()[hijri[Calendar.MONTH]]
        val hijriStr = "$hDayName ${hijri[Calendar.DATE]} $hMonth ${hijri[Calendar.YEAR]}"
        return LangUtils.translateNums(repository.numeralsLanguage, hijriStr)
    }

    private fun getGregorianDate(): String {
        val gregorian = Calendar.getInstance()
        val mMonth = repository.getGregorianMonths()[gregorian[Calendar.MONTH]]
        val gregorianStr = "${gregorian[Calendar.DATE]} $mMonth ${gregorian[Calendar.YEAR]}"
        return LangUtils.translateNums(repository.numeralsLanguage, gregorianStr)
    }

    private fun setupBootReceiver() {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, DeviceBootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun onDateEditorNextDay() {
        dateOffset++
        updateDateEditor()
    }

    fun onDateEditorPrevDay() {
        dateOffset--
        updateDateEditor()
    }

    private fun updateDateEditor() {
        val cal = UmmalquraCalendar()
        val millisInDay = 1000 * 60 * 60 * 24
        cal.timeInMillis = cal.timeInMillis + dateOffset * millisInDay

        // update tvs
        val dateText = LangUtils.translateNums(
            context,
            "${cal[Calendar.DATE]}/${cal[Calendar.MONTH] + 1}/${cal[Calendar.YEAR]}"
        )

        var offsetText = context.getString(R.string.unchanged)
        if (dateOffset != 0) {
            var offsetStr = dateOffset.toString()
            if (dateOffset > 0) offsetStr = "+$offsetStr"
            offsetText = LangUtils.translateNums(context, offsetStr)
        }

        _uiState.update { it.copy(
            dateEditorOffsetText = offsetText,
            dateEditorDateText = dateText
        )}
    }

    fun onDateEditorSubmit() {
        _uiState.update { it.copy(
            hijriDate = getHijriDate(),
            gregorianDate = getGregorianDate(),
            dateEditorShown = false
        )}

        repository.updateDateOffset(dateOffset)
    }

    fun onDateEditorCancel() {
        _uiState.update { it.copy(
            dateEditorShown = false
        )}
    }

}