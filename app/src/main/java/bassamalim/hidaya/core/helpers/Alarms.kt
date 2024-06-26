package bassamalim.hidaya.core.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.receivers.NotificationReceiver
import bassamalim.hidaya.core.utils.DBUtils
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.core.utils.PrefUtils
import java.util.*

class Alarms {

    private val context: Context
    private var sp: SharedPreferences

    constructor(gContext: Context, gTimes: Array<Calendar?>) {
        context = gContext
        sp = PrefUtils.getPreferences(context)

        setAll(gTimes)
    }

    constructor(gContext: Context, pid: PID) {
        context = gContext
        sp = PrefUtils.getPreferences(context)

        val times = PTUtils.getTimes(sp, DBUtils.getDB(context)) ?: return

        if (pid.ordinal in 0..5) {
            setPrayerAlarm(pid = pid, time = times[pid.ordinal]!!)

            val reminderOffset = PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))
            if (reminderOffset != 0)
                setReminder(pid = pid, time = times[pid.ordinal]!!, offset = reminderOffset)
        }
        else if (pid.ordinal in 6..9) setExtraAlarm(pid)
    }

    /**
     * Finds out if the desired function and executes it
     */
    private fun setAll(times: Array<Calendar?>) {
        setPrayerAlarms(times)
        setReminders(times)
        setExtraAlarms()
    }

    private fun setPrayerAlarms(times: Array<Calendar?>) {
        Log.i(Global.TAG, "in set prayer alarms")

        val pidValues = PID.entries.toTypedArray()
        for (i in times.indices) {
            val pid = pidValues[i]
            if (PrefUtils.getString(sp, Prefs.NotificationType(pid)) != NotificationType.None.name)
                setPrayerAlarm(pid, times[i]!!)
        }
    }

    /**
     * Set an alarm for the given prayer time
     *
     * @param pid the ID of the prayer
     */
    private fun setPrayerAlarm(pid: PID, time: Calendar) {
        Log.i(Global.TAG, "in set alarm for: $pid")

        val millis = time.timeInMillis
        if (System.currentTimeMillis() <= millis) {
            val intent = Intent(context, NotificationReceiver::class.java)
            if (pid == PID.SUNRISE) intent.action = "extra"
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

    private fun setReminders(times: Array<Calendar?>) {
        Log.i(Global.TAG, "in set reminders")

        val pidValues = PID.entries.toTypedArray()
        for (i in times.indices) {
            val pid = pidValues[i]
            val offset = PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))
            if (offset != 0) setReminder(pid, times[i]!!, offset)
        }
    }

    private fun setReminder(pid: PID, time: Calendar, offset: Int) {
        Log.i(Global.TAG, "in set reminder for: $pid")

        val millis = time.timeInMillis + offset * 60 * 1000
        if (System.currentTimeMillis() <= millis) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "reminder"
                putExtra("id", pid.name)
                putExtra("time", millis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, pid.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

            Log.i(Global.TAG, "reminder $pid set")
        }
        else Log.i(Global.TAG, "reminder $pid Passed")
    }

    /**
     * Set the extra alarms based on the user preferences
     */
    private fun setExtraAlarms() {
        Log.i(Global.TAG, "in set extra alarms")

        val today = Calendar.getInstance()

        if (PrefUtils.getBoolean(sp, Prefs.NotifyExtraNotification(PID.MORNING)))
            setExtraAlarm(PID.MORNING)
        if (PrefUtils.getBoolean(sp, Prefs.NotifyExtraNotification(PID.EVENING)))
            setExtraAlarm(PID.EVENING)
        if (PrefUtils.getBoolean(sp, Prefs.NotifyExtraNotification(PID.DAILY_WERD)))
            setExtraAlarm(PID.DAILY_WERD)
        if (PrefUtils.getBoolean(sp, Prefs.NotifyExtraNotification(PID.FRIDAY_KAHF))
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

        val hour = PrefUtils.getInt(sp, Prefs.ExtraNotificationHour(pid))
        val minute = PrefUtils.getInt(sp, Prefs.ExtraNotificationMinute(pid))

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