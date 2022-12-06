package bassamalim.hidaya.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.NotificationType
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.receivers.NotificationReceiver
import bassamalim.hidaya.utils.PTUtils
import java.util.*

class Alarms {

    private val context: Context
    private var pref: SharedPreferences

    constructor(gContext: Context, gTimes: Array<Calendar?>) {
        context = gContext
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        setAll(gTimes)
    }

    constructor(gContext: Context, pid: PID) {
        context = gContext
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        val times = PTUtils.getTimes(context) ?: return

        if (pid.ordinal in 0..5) setPrayerAlarm(pid, times[pid.ordinal])
        else if (pid.ordinal in 6..9) setExtraAlarm(pid)
    }

    /**
     * Finds out if the desired function and executes it
     */
    private fun setAll(times: Array<Calendar?>) {
        setPrayerAlarms(times)
        setExtraAlarms()
    }

    private fun setPrayerAlarms(times: Array<Calendar?>) {
        Log.i(Global.TAG, "in set prayer alarms")
        val pidValues = PID.values()
        for (i in times.indices) {
            val pid = pidValues[i]
            if (pref.getString(
                    "$pid notification_type", NotificationType.Notification.name
                ) != NotificationType.None.name)
                setPrayerAlarm(pid, times[i])
        }
    }

    /**
     * Set an alarm for the given prayer time
     *
     * @param pid the ID of the prayer
     */
    private fun setPrayerAlarm(pid: PID, time: Calendar?) {
        Log.i(Global.TAG, "in set alarm for: $pid")

        val millis = time!!.timeInMillis
        if (System.currentTimeMillis() <= millis) {
            val intent = Intent(context, NotificationReceiver::class.java)
            if (pid == PID.SHOROUQ) intent.action = "extra"
            else intent.action = "prayer"
            intent.putExtra("id", pid.name)
            intent.putExtra("time", millis)

            val pendingIntent = PendingIntent.getBroadcast(
                context, pid.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

            Log.i(Global.TAG, "alarm $pid set")
        }
        else Log.i(Global.TAG, "$pid Passed")
    }

    /**
     * Set the extra alarms based on the user preferences
     */
    private fun setExtraAlarms() {
        Log.i(Global.TAG, "in set extra alarms")

        val today = Calendar.getInstance()

        if (pref.getBoolean(context.getString(R.string.morning_athkar_key), true)) setExtraAlarm(PID.MORNING)
        if (pref.getBoolean(context.getString(R.string.evening_athkar_key), true)) setExtraAlarm(PID.EVENING)
        if (pref.getBoolean(context.getString(R.string.daily_werd_key), true)) setExtraAlarm(PID.DAILY_WERD)
        if (pref.getBoolean(context.getString(R.string.friday_kahf_key), true)
            && today[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            setExtraAlarm(PID.FRIDAY_KAHF)
    }

    /**
     * Set an alarm for a specific time
     *
     * @param pid The ID of the alarm.
     */
    private fun setExtraAlarm(pid: PID) {
        Log.i(Global.TAG, "in set extra alarm")

        var defaultH = 0
        val defaultM = 0
        when (pid) {
            PID.MORNING -> defaultH = 5
            PID.EVENING -> defaultH = 16
            PID.DAILY_WERD -> defaultH = 21
            PID.FRIDAY_KAHF -> defaultH = 13
            else -> {}
        }

        val hour = pref.getInt("$pid hour", defaultH)
        val minute = pref.getInt("$pid minute", defaultM)

        val time = Calendar.getInstance()
        time[Calendar.HOUR_OF_DAY] = hour
        time[Calendar.MINUTE] = minute
        time[Calendar.SECOND] = 0
        time[Calendar.MILLISECOND] = 0

        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = "extra"
        intent.putExtra("id", pid.name)
        intent.putExtra("time", time.timeInMillis)

        val myAlarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = PendingIntent.getBroadcast(
            context, pid.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)

        Log.i(Global.TAG, "alarm $pid set")
    }

}