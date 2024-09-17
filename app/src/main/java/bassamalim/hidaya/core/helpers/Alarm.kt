package bassamalim.hidaya.core.helpers

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PrayerTimePoint
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.receivers.NotificationReceiver
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.SortedMap

class Alarm(
    private val app: Application,
    private val prayersRepository: PrayersRepository,
    private val notificationsRepository: NotificationsRepository,
    private val locationRepository: LocationRepository
) {

    suspend fun setReminderAlarm(reminder: Reminder) {
        val location = locationRepository.getLocation().first() ?: return
        val prayerTimesMap = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )

        when (reminder) {
            is Reminder.Prayer -> {
                setPrayerAlarm(reminder = reminder, time = prayerTimesMap[reminder]!!)

                val reminderOffset = notificationsRepository.getPrayerReminderOffsetMap().first()[reminder]!!
                if (reminderOffset != 0)
                    setReminder(reminder = reminder, time = prayerTimesMap[reminder]!!, offset = reminderOffset)
            }
            is Reminder.Devotional -> {
                setDevotionAlarm(reminder)
            }
            else -> {}
        }
    }

    /**
     * Finds out if the desired function and executes it
     */
    suspend fun setAll(prayerTimes: SortedMap<PrayerTimePoint, Calendar?>) {
        setPrayerAlarms(prayerTimes)
        setReminders(prayerTimes)
        setDevotionAlarms()
    }

    private suspend fun setPrayerAlarms(prayerTimes: SortedMap<PrayerTimePoint, Calendar?>) {
        Log.i(Global.TAG, "in set prayer alarms")

        for ((pid, time) in prayerTimes) {
            if (notificationsRepository.getNotificationType(pid).first() != NotificationType.NONE)
                setPrayerAlarm(pid, time!!)
        }
    }

    /**
     * Set an alarm for the given prayer time
     *
     * @param reminder the ID of the prayer
     */
    private fun setPrayerAlarm(reminder: Reminder, time: Calendar) {
        Log.i(Global.TAG, "in set alarm for: $reminder")

        val millis = time.timeInMillis
        if (System.currentTimeMillis() <= millis) {
            val intent = Intent(app, NotificationReceiver::class.java).also {
                it.action = if (reminder == Reminder.Prayer.Sunrise) "devotion" else "prayer"
                it.putExtra("id", reminder.toString())
                it.putExtra("time", millis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                app, reminder.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

            Log.i(Global.TAG, "alarm $reminder set")
        }
        else Log.i(Global.TAG, "$reminder Passed")
    }

    private suspend fun setReminders(prayerTimes: SortedMap<PrayerTimePoint, Calendar?>) {
        Log.i(Global.TAG, "in set reminders")

        for ((pid, time) in prayerTimes) {
            val reminderOffset = notificationsRepository.getPrayerReminderOffsetMap().first()[pid]!!
            if (reminderOffset != 0) setReminder(pid, time!!, reminderOffset)
        }
    }

    private fun setReminder(reminder: Reminder, time: Calendar, offset: Int) {
        Log.i(Global.TAG, "in set reminder for: $reminder")

        val millis = time.timeInMillis + offset * 60 * 1000
        if (System.currentTimeMillis() <= millis) {
            val intent = Intent(app, NotificationReceiver::class.java).apply {
                action = "reminder"
                putExtra("id", reminder.name)
                putExtra("time", millis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                app, reminder.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

            Log.i(Global.TAG, "reminder $reminder set")
        }
        else Log.i(Global.TAG, "reminder $reminder Passed")
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
                if (pid == Reminder.FRIDAY_KAHF && today[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                    setDevotionAlarm(pid)
                else if (pid != Reminder.FRIDAY_KAHF) setDevotionAlarm(pid)
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

        val timeOfDay = notificationsRepository.getDevotionReminderTimes().first()[pid]!!

        val time = Calendar.getInstance()
        time[Calendar.HOUR_OF_DAY] = timeOfDay.hour
        time[Calendar.MINUTE] = timeOfDay.minute
        time[Calendar.SECOND] = 0
        time[Calendar.MILLISECOND] = 0

        val intent = Intent(app, NotificationReceiver::class.java)
        intent.action = "devotion"
        intent.putExtra("id", pid.name)
        intent.putExtra("time", time.timeInMillis)

        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = PendingIntent.getBroadcast(
            app, pid.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time.timeInMillis,
            pendingIntent
        )

        Log.i(Global.TAG, "alarm $pid set")
    }

    fun cancelAlarm(pid: PID) {
        val pendingIntent = PendingIntent.getBroadcast(
            app, pid.ordinal, Intent(),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.i(Global.TAG, "Canceled $pid Alarm")
    }

}