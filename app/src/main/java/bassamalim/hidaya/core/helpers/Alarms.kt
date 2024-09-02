package bassamalim.hidaya.core.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.receivers.NotificationReceiver
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import dagger.hilt.EntryPoint
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.SortedMap
import javax.inject.Inject

@EntryPoint
class Alarms(
    private val context: Context
) {

    @Inject private lateinit var prayersRepository: PrayersRepository
    @Inject private lateinit var notificationsRepository: NotificationsRepository
    @Inject private lateinit var locationRepository: LocationRepository

    suspend fun setPidAlarm(pid: PID) {
        val location = locationRepository.getLocation().first() ?: return
        val prayerTimesMap = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )

        if (pid.ordinal in 0..5) {
            setPrayerAlarm(pid = pid, time = prayerTimesMap[pid]!!)

            val reminderOffset = notificationsRepository.getPrayerReminderOffsetMap().first()[pid]!!
            if (reminderOffset != 0)
                setReminder(pid = pid, time = prayerTimesMap[pid]!!, offset = reminderOffset)
        }
        else if (pid.ordinal in 6..9) setDevotionAlarm(pid)
    }

    /**
     * Finds out if the desired function and executes it
     */
    suspend fun setAll(prayerTimeMap: SortedMap<PID, Calendar?>) {
        setPrayerAlarms(prayerTimeMap)
        setReminders(prayerTimeMap)
        setDevotionAlarms()
    }

    private suspend fun setPrayerAlarms(prayerTimeMap: SortedMap<PID, Calendar?>) {
        Log.i(Global.TAG, "in set prayer alarms")

        for ((pid, time) in prayerTimeMap) {
            if (notificationsRepository.getNotificationType(pid).first() != NotificationType.NONE)
                setPrayerAlarm(pid, time!!)
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
            if (pid == PID.SUNRISE) intent.action = "devotion"
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

    private suspend fun setReminders(prayerTimeMap: SortedMap<PID, Calendar?>) {
        Log.i(Global.TAG, "in set reminders")

        for ((pid, time) in prayerTimeMap) {
            val reminderOffset = notificationsRepository.getPrayerReminderOffsetMap().first()[pid]!!
            if (reminderOffset != 0) setReminder(pid, time!!, reminderOffset)
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

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

            Log.i(Global.TAG, "reminder $pid set")
        }
        else Log.i(Global.TAG, "reminder $pid Passed")
    }

    /**
     * Set the devotion alarms based on the user preferences
     */
    private suspend fun setDevotionAlarms() {
        Log.i(Global.TAG, "in set devotion alarms")

        val today = Calendar.getInstance()

        val devotionAlarmEnabledMap =
            notificationsRepository.getDevotionReminderEnabledMap().first()

        for ((pid, enabled) in devotionAlarmEnabledMap) {
            if (enabled) {
                if (pid == PID.FRIDAY_KAHF && today[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                    setDevotionAlarm(pid)
                else if (pid != PID.FRIDAY_KAHF) setDevotionAlarm(pid)
            }
        }
    }

    /**
     * Set an alarm for a specific time
     *
     * @param pid The ID of the alarm.
     */
    private suspend fun setDevotionAlarm(pid: PID) {
        Log.i(Global.TAG, "in set extra alarm")

        val timeOfDay = notificationsRepository.getDevotionReminderTimeOfDayMap().first()[pid]!!

        val time = Calendar.getInstance()
        time[Calendar.HOUR_OF_DAY] = timeOfDay.hour
        time[Calendar.MINUTE] = timeOfDay.minute
        time[Calendar.SECOND] = 0
        time[Calendar.MILLISECOND] = 0

        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = "devotion"
        intent.putExtra("id", pid.name)
        intent.putExtra("time", time.timeInMillis)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = PendingIntent.getBroadcast(
            context, pid.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time.timeInMillis,
            pendingIntent
        )

        Log.i(Global.TAG, "alarm $pid set")
    }

}