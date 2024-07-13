package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.NotificationsPreferences
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object NotificationsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.NOTIFICATIONS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: NotificationsPreferences ->
            currentData.copy(
                notificationTypes = persistentMapOf<PID, NotificationType>().mutate {
                    PID.entries.map {
                        it to NotificationType.valueOf(
                            sharedPrefs.getString(
                                key = Preference.NotificationType(it).key,
                                defValue = Preference.NotificationType(it).default as String
                            )!!
                        )
                    }
                },
                extraNotificationsMinuteOfDay = persistentMapOf(
                    PID.MORNING to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.MORNING).key,
                        defValue = Preference.ExtraNotificationHour(PID.MORNING).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.MORNING).key,
                        defValue = Preference.ExtraNotificationMinute(PID.MORNING).default as Int
                    ),
                    PID.EVENING to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.EVENING).key,
                        defValue = Preference.ExtraNotificationHour(PID.EVENING).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.EVENING).key,
                        defValue = Preference.ExtraNotificationMinute(PID.EVENING).default as Int
                    ),
                    PID.DAILY_WERD to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.DAILY_WERD).key,
                        defValue = Preference.ExtraNotificationHour(PID.DAILY_WERD).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.DAILY_WERD).key,
                        defValue = Preference.ExtraNotificationMinute(PID.DAILY_WERD).default as Int
                    ),
                    PID.FRIDAY_KAHF to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.FRIDAY_KAHF).key,
                        defValue = Preference.ExtraNotificationHour(PID.FRIDAY_KAHF).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.FRIDAY_KAHF).key,
                        defValue = Preference.ExtraNotificationMinute(PID.FRIDAY_KAHF).default as Int
                    )
                ),
                notifyExtraNotifications = persistentMapOf(
                    PID.MORNING to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.MORNING).key,
                        defValue = Preference.NotifyExtraNotification(PID.MORNING).default as Boolean
                    ),
                    PID.EVENING to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.EVENING).key,
                        defValue = Preference.NotifyExtraNotification(PID.EVENING).default as Boolean
                    ),
                    PID.DAILY_WERD to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.DAILY_WERD).key,
                        defValue = Preference.NotifyExtraNotification(PID.DAILY_WERD).default as Boolean
                    ),
                    PID.FRIDAY_KAHF to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.FRIDAY_KAHF).key,
                        defValue = Preference.NotifyExtraNotification(PID.FRIDAY_KAHF).default as Boolean
                    )
                ),
                prayerReminderOffsets = persistentMapOf(
                    PID.FAJR to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.FAJR).key,
                        defValue = Preference.ReminderOffset(PID.FAJR).default as Int
                    ),
                    PID.SUNRISE to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.SUNRISE).key,
                        defValue = Preference.ReminderOffset(PID.SUNRISE).default as Int
                    ),
                    PID.DHUHR to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.DHUHR).key,
                        defValue = Preference.ReminderOffset(PID.DHUHR).default as Int
                    ),
                    PID.ASR to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.ASR).key,
                        defValue = Preference.ReminderOffset(PID.ASR).default as Int
                    ),
                    PID.MAGHRIB to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.MAGHRIB).key,
                        defValue = Preference.ReminderOffset(PID.MAGHRIB).default as Int
                    ),
                    PID.ISHAA to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.ISHAA).key,
                        defValue = Preference.ReminderOffset(PID.ISHAA).default as Int
                    ),
                ),
                lastNotificationDates = persistentMapOf<PID, Int>().mutate {
                    PID.entries.map {
                        it to 0
                    }
                },
            )
        }

}