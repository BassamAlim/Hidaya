package bassamalim.hidaya.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
import bassamalim.hidaya.repository.MainRepo
import bassamalim.hidaya.state.MainState
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils.translateNums
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
    private val app: Application,
    private val repository: MainRepo
): AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(MainState(
        hijriDate = getHijriDate(),
        gregorianDate = getGregorianDate()
    ))
    val uiState = _uiState.asStateFlow()

    private var dateOffset = repository.getDateOffset()

    init {
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
        val ctx = app.applicationContext
        val location = repository.getLocation()
        if (location != null) {
            val times = PTUtils.getTimes(
                repository.pref,
                DBUtils.getDB(ctx)
            )!!
            Alarms(ctx, times)
        }
        else {
            _uiState.update { it.copy(
                shouldShowLocationPermissionToast = true
            )}
        }
    }

    private fun dailyUpdate() {
        val ctx = app.applicationContext
        val intent = Intent(ctx, DailyUpdateReceiver::class.java)
        intent.action = "daily"
        ctx.sendBroadcast(intent)
    }

    private fun getHijriDate(): String {
        val hijri = UmmalquraCalendar()
        val hDayName = repository.getWeekDays()[hijri[Calendar.DAY_OF_WEEK] - 1]

        val millisInDay = 1000 * 60 * 60 * 24
        hijri.timeInMillis = hijri.timeInMillis + dateOffset * millisInDay

        val hMonth = repository.getHijriMonths()[hijri[Calendar.MONTH]]
        val hijriStr = "$hDayName ${hijri[Calendar.DATE]} $hMonth ${hijri[Calendar.YEAR]}"
        return translateNums(repository.numeralsLanguage, hijriStr)
    }

    private fun getGregorianDate(): String {
        val gregorian = Calendar.getInstance()
        val mMonth = repository.getGregorianMonths()[gregorian[Calendar.MONTH]]
        val gregorianStr = "${gregorian[Calendar.DATE]} $mMonth ${gregorian[Calendar.YEAR]}"
        return translateNums(repository.numeralsLanguage, gregorianStr)
    }

    private fun setupBootReceiver() {
        val ctx = app.applicationContext
        ctx.packageManager.setComponentEnabledSetting(
            ComponentName(ctx, DeviceBootReceiver::class.java),
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
        val dateText = translateNums(
            repository.numeralsLanguage,
            "${cal[Calendar.DATE]}/${cal[Calendar.MONTH] + 1}/${cal[Calendar.YEAR]}"
        )

        var offsetText = repository.getUnchangedStr()
        if (dateOffset != 0) {
            var offsetStr = dateOffset.toString()
            if (dateOffset > 0) offsetStr = "+$offsetStr"
            offsetText = translateNums(repository.numeralsLanguage, offsetStr)
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