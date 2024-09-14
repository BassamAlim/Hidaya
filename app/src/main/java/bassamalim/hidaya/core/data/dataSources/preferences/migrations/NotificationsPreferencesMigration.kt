package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.NotificationsPreferences
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

object NotificationsPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.NOTIFICATIONS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: NotificationsPreferences ->
            currentData.copy(
                notificationTypes = persistentMapOf<PID, NotificationType>().mutate {
                    PID.entries.map {
                        it to NotificationType.valueOf(
                            sharedPrefs.getString(
                                key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotificationType(it).key,
                                defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotificationType(it).default as String
                            )!!
                        )
                    }
                },
                devotionalReminderTimes = listOf(
                    PID.MORNING, PID.EVENING, PID.DAILY_WERD, PID.FRIDAY_KAHF
                ).associateWith { pid ->
                    TimeOfDay(
                        hour = sharedPrefs.getInt(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ExtraNotificationHour(pid).key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ExtraNotificationHour(pid).default as Int
                        ),
                        minute = sharedPrefs.getInt(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ExtraNotificationMinute(pid).key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ExtraNotificationMinute(pid).default as Int
                        )
                    )
                }.toPersistentMap(),
                devotionalReminderEnabled = persistentMapOf(
                    PID.MORNING to sharedPrefs.getBoolean(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.MORNING).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.MORNING).default as Boolean
                    ),
                    PID.EVENING to sharedPrefs.getBoolean(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.EVENING).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.EVENING).default as Boolean
                    ),
                    PID.DAILY_WERD to sharedPrefs.getBoolean(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.DAILY_WERD).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.DAILY_WERD).default as Boolean
                    ),
                    PID.FRIDAY_KAHF to sharedPrefs.getBoolean(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.FRIDAY_KAHF).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.NotifyExtraNotification(PID.FRIDAY_KAHF).default as Boolean
                    )
                ),
                prayerReminderTimeOffsets = persistentMapOf(
                    PID.FAJR to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.FAJR).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.FAJR).default as Int
                    ),
                    PID.SUNRISE to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.SUNRISE).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.SUNRISE).default as Int
                    ),
                    PID.DHUHR to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.DHUHR).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.DHUHR).default as Int
                    ),
                    PID.ASR to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.ASR).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.ASR).default as Int
                    ),
                    PID.MAGHRIB to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.MAGHRIB).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.MAGHRIB).default as Int
                    ),
                    PID.ISHAA to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.ISHAA).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ReminderOffset(PID.ISHAA).default as Int
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