package bassamalim.hidaya.features.settings

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.os.Message
import android.widget.TimePicker
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.utils.DBUtils
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.core.utils.PTUtils.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SettingsVM @Inject constructor(
    private val app: Application,
    private val repo: SettingsRepo
): AndroidViewModel(app) {

    val sp = repo.sp

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        setSummaries()
    }

    private fun setSummaries() {
        _uiState.update { it.copy(
            morningSummary = translateNums(
                repo.numeralsLanguage,
                formatTime(
                    repo.timeFormat,
                    repo.getTime(PID.MORNING)
                ),
                timeFormat = true
            ),
            eveningSummary = translateNums(
                repo.numeralsLanguage,
                formatTime(
                    repo.timeFormat,
                    repo.getTime(PID.EVENING)
                ),
                timeFormat = true
            ),
            werdSummary = translateNums(
                repo.numeralsLanguage,
                formatTime(
                    repo.timeFormat,
                    repo.getTime(PID.DAILY_WERD)
                ),
                timeFormat = true
            ),
            kahfSummary = translateNums(
                repo.numeralsLanguage,
                formatTime(
                    repo.timeFormat,
                    repo.getTime(PID.FRIDAY_KAHF)
                ),
                timeFormat = true
            ),
        )}
    }

    fun onSwitch(ctx: Context, checked: Boolean, pid: PID) {
        if (checked) showTimePicker(ctx, pid)
        else cancelAlarm(pid)
    }

    fun onPrayerTimesCalculationMethodCh() {
        resetPrayerTimes()
    }

    fun onPrayerTimesJuristicMethodCh() {
        resetPrayerTimes()
    }

    fun onPrayerTimesHighLatAdjustmentCh() {
        resetPrayerTimes()
    }

    private fun showTimePicker(ctx: Context, pid: PID) {
        val currentTime = Calendar.getInstance()
        val cHour = currentTime[Calendar.HOUR_OF_DAY]
        val cMinute = currentTime[Calendar.MINUTE]

        val timePicker = TimePickerDialog(
            ctx, { _: TimePicker?, hourOfDay: Int, minute: Int ->
                updateSummary(pid, hourOfDay, minute)

                repo.setTime(pid, hourOfDay, minute)

                Alarms(app, pid)
            }, cHour, cMinute, false
        )

        timePicker.setOnCancelListener { setSummaries() }
        timePicker.setOnDismissListener { setSummaries() }
        timePicker.setTitle(repo.getTimePickerTitleStr())
        timePicker.setButton(
            TimePickerDialog.BUTTON_POSITIVE,
            repo.getSelectStr(),
            null as Message?
        )
        timePicker.setButton(
            TimePickerDialog.BUTTON_NEGATIVE,
            repo.getCancelStr(),
            null as Message?
        )
        timePicker.setCancelable(true)
        timePicker.show()
    }

    private fun updateSummary(pid: PID, hour: Int, minute: Int) {
        when (pid) {
            PID.MORNING -> _uiState.update { it.copy(
                morningSummary = translateNums(
                    repo.numeralsLanguage,
                    formatTime(
                        repo.timeFormat,
                        "$hour:$minute"
                    ),
                    true
                )
            )}
            PID.EVENING -> _uiState.update { it.copy(
                eveningSummary = translateNums(
                    repo.numeralsLanguage,
                    formatTime(
                        repo.timeFormat,
                        "$hour:$minute"
                    ),
                    true
                )
            )}
            PID.DAILY_WERD -> _uiState.update { it.copy(
                werdSummary = translateNums(
                    repo.numeralsLanguage,
                    formatTime(
                        repo.timeFormat,
                        "$hour:$minute"
                    ),
                    true
                )
            )}
            PID.FRIDAY_KAHF -> _uiState.update { it.copy(
                kahfSummary = translateNums(
                    repo.numeralsLanguage,
                    formatTime(
                        repo.timeFormat,
                        "$hour:$minute"
                    ),
                    true
                )
            )}
            else -> {}
        }
    }

    private fun cancelAlarm(pid: PID) {
        PTUtils.cancelAlarm(app, pid)

        when (pid) {
            PID.MORNING -> _uiState.update { it.copy(
                morningSummary = ""
            )}
            PID.EVENING -> _uiState.update { it.copy(
                eveningSummary = ""
            )}
            PID.DAILY_WERD -> _uiState.update { it.copy(
                werdSummary = ""
            )}
            PID.FRIDAY_KAHF -> _uiState.update { it.copy(
                kahfSummary = ""
            )}
            else -> {}
        }
    }

    private fun resetPrayerTimes() {
        val prayerTimes = PTUtils.getTimes(sp, DBUtils.getDB(app))
        if (prayerTimes != null) Alarms(app, prayerTimes)
    }

}