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
import bassamalim.hidaya.core.enums.Prayer
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

    /**
     * Finds out if the desired function and executes it
     */
    suspend fun setAll(prayerTimes: SortedMap<Prayer, Calendar?>) {
        val reminderTimes = prayerTimes.map { (prayer, time) ->
            prayer.toReminder() to time
        }.toMap()

        setPrayerAlarms(reminderTimes)
        setPrayerExtraReminderAlarms(reminderTimes)
        setDevotionAlarms()
    }

    suspend fun setAlarm(reminder: Reminder) {
        when (reminder) {
            is Reminder.Prayer -> {
                val location = locationRepository.getLocation().first() ?: return
                val prayerTimes = PrayerTimeUtils.getPrayerTimes(
                    settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                    timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
                    location = location,
                    calendar = Calendar.getInstance()
                ).map { (prayer, time) -> prayer.toReminder() to time }.toMap()

                setPrayerAlarm(reminder = reminder, time = prayerTimes[reminder]!!)

                setPrayerReminder(
                    reminder = reminder,
                    time = prayerTimes[reminder]!!
                )
            }
            is Reminder.PrayerExtra -> {
                val location = locationRepository.getLocation().first() ?: return
                val prayerTime = PrayerTimeUtils.getPrayerTimes(
                    settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                    timeZoneId = locationRepository.getTimeZone(location.ids.cityId),
                    location = location,
                    calendar = Calendar.getInstance()
                )[reminder.toPrayer()]!!

                setPrayerExtraReminderAlarm(
                    reminder = reminder,
                    time = prayerTime,
                    offset = notificationsRepository.getPrayerExtraReminderTimeOffsets()
                        .first()[reminder]!!
                )
            }
            is Reminder.Devotional -> {
                setDevotionAlarm(reminder)
            }
        }
    }

    private suspend fun setPrayerAlarms(prayerTimes: Map<Reminder.Prayer, Calendar?>) {
        Log.i(Global.TAG, "in set prayer alarms")

        for ((prayer, time) in prayerTimes) {
            if (notificationsRepository.getNotificationType(prayer).first() != NotificationType.NONE)
                setPrayerAlarm(prayer, time!!)
        }
    }

    /**
     * Set an alarm for the given prayer time
     *
     * @param reminder the ID of the prayer
     */
    private fun setPrayerAlarm(reminder: Reminder.Prayer, time: Calendar) {
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

    private suspend fun setPrayerExtraReminderAlarms(prayerTimes: Map<Reminder.Prayer, Calendar?>) {
        Log.i(Global.TAG, "in setPrayerExtraReminderAlarms")

        val reminderOffsets = notificationsRepository.getPrayerExtraReminderTimeOffsets().first()
        for ((prayer, time) in prayerTimes) {
            val prayerExtra = prayer.toPrayerExtra()
            val reminderOffset = reminderOffsets[prayerExtra]!!
            if (reminderOffset != 0)
                setPrayerExtraReminderAlarm(prayerExtra, time!!, reminderOffset)
        }
    }

    fun setPrayerExtraReminderAlarm(
        reminder: Reminder.PrayerExtra,
        time: Calendar,
        offset: Int
    ) {
        Log.i(Global.TAG, "in setPrayerExtraReminderAlarm for: $reminder")

        val millis = time.timeInMillis + offset * 1000
        if (System.currentTimeMillis() <= millis) {
            val intent = Intent(app, NotificationReceiver::class.java).apply {
                action = "prayer_extra"
                putExtra("id", reminder.name)
                putExtra("time", millis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                app, reminder.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)

            Log.i(Global.TAG, "prayer extra reminder $reminder set")
        }
        else Log.i(Global.TAG, "prayer extra reminder $reminder Passed")
    }

    private fun setPrayerReminder(reminder: Reminder.Prayer, time: Calendar) {
        Log.i(Global.TAG, "in set reminder for: $reminder")

        if (System.currentTimeMillis() <= time.timeInMillis) {
            val intent = Intent(app, NotificationReceiver::class.java).apply {
                action = "prayer"
                putExtra("id", reminder.name)
                putExtra("time", time.timeInMillis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                app, reminder.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time.timeInMillis,
                pendingIntent
            )

            Log.i(Global.TAG, "prayer reminder $reminder set")
        }
        else Log.i(Global.TAG, "prayer reminder $reminder Passed")
    }

    /**
     * Set the devotion alarms based on the user preferences
     */
    private suspend fun setDevotionAlarms() {
        Log.i(Global.TAG, "in set devotion alarms")

        val today = Calendar.getInstance()

        val devotionAlarmEnabledMap =
            notificationsRepository.getDevotionalReminderEnabledMap().first()

        for ((devotion, enabled) in devotionAlarmEnabledMap) {
            if (enabled) {
                when (devotion) {
                    is Reminder.Devotional.FridayKahf -> {
                        if (today[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                            setDevotionAlarm(devotion)
                    }
                    else -> setDevotionAlarm(devotion)
                }
            }
        }
    }

    /**
     * Set an alarm for a specific time
     *
     * @param devotion The ID of the alarm.
     */
    private suspend fun setDevotionAlarm(devotion: Reminder.Devotional) {
        Log.i(Global.TAG, "in set extra alarm")

        val timeOfDay = notificationsRepository.getDevotionalReminderTimes().first()[devotion]!!

        val time = Calendar.getInstance()
        time[Calendar.HOUR_OF_DAY] = timeOfDay.hour
        time[Calendar.MINUTE] = timeOfDay.minute
        time[Calendar.SECOND] = 0
        time[Calendar.MILLISECOND] = 0

        val intent = Intent(app, NotificationReceiver::class.java)
        intent.action = "devotion"
        intent.putExtra("id", devotion.name)
        intent.putExtra("time", time.timeInMillis)

        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = PendingIntent.getBroadcast(
            app, devotion.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time.timeInMillis,
            pendingIntent
        )

        Log.i(Global.TAG, "alarm $devotion set")
    }

    fun cancelAlarm(reminder: Reminder) {
        val pendingIntent = PendingIntent.getBroadcast(
            app, reminder.id, Intent(),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.i(Global.TAG, "Canceled $reminder Alarm")
    }

}