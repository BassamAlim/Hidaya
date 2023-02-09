package bassamalim.hidaya.viewmodel

import android.app.Application
import android.app.TimePickerDialog
import android.content.Context
import android.os.Message
import android.widget.TimePicker
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.repository.SettingsRepo
import bassamalim.hidaya.state.SettingsState
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils.translateNums
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PTUtils.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsVM @Inject constructor(
    private val app: Application,
    private val repo: SettingsRepo
): AndroidViewModel(app) {

    val pref = repo.pref

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

    private fun showTimePicker(ctx: Context, pid: PID) {
        val currentTime = Calendar.getInstance()
        val cHour = currentTime[Calendar.HOUR_OF_DAY]
        val cMinute = currentTime[Calendar.MINUTE]

        val timePicker = TimePickerDialog(
            ctx, { _: TimePicker?, hourOfDay: Int, minute: Int ->
                updateSummary(pid, hourOfDay, minute)

                repo.setTime(pid, hourOfDay, minute)

                Alarms(app.applicationContext, pid)
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

    fun resetPrayerTimes() {
        val ctx = app.applicationContext

        val prayerTimes = PTUtils.getTimes(pref, DBUtils.getDB(ctx))
        if (prayerTimes != null) Alarms(ctx, prayerTimes)
    }

    private fun cancelAlarm(pid: PID) {
        PTUtils.cancelAlarm(app.applicationContext, pid)

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

}