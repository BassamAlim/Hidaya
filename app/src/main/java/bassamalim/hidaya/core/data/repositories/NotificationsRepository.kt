package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val notificationsPreferencesDataSource: NotificationsPreferencesDataSource,
    @ApplicationScope private val scope: CoroutineScope
) {

    fun getNotificationTypes() = notificationsPreferencesDataSource.getNotificationTypes().map {
        it.toMap()
    }

    fun getNotificationType(prayer: Reminder.Prayer) = getNotificationTypes().map { it[prayer]!! }

    fun setNotificationType(type: NotificationType, prayer: Reminder.Prayer) {
        scope.launch {
            notificationsPreferencesDataSource.updateNotificationTypes(
                notificationsPreferencesDataSource.getNotificationTypes().first().mutate {
                    it[prayer] = type
                }
            )
        }
    }

    fun getDevotionalReminderEnabledMap() =
        notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses().map {
            it.toMap()
        }

    fun setDevotionalReminderEnabled(enabled: Boolean, devotion: Reminder.Devotional) {
        scope.launch {
            notificationsPreferencesDataSource.updateDevotionalReminderEnabledStatuses(
                notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses().first()
                    .mutate { it[devotion] = enabled }
            )
        }
    }

    fun getDevotionalReminderTimes() =
        notificationsPreferencesDataSource.getDevotionalReminderTimes().map {
            it.toMap()
        }

    fun setDevotionalReminderTimes(timeOfDay: TimeOfDay, devotion: Reminder.Devotional) {
        scope.launch {
            notificationsPreferencesDataSource.updateDevotionalReminderTimes(
                notificationsPreferencesDataSource.getDevotionalReminderTimes().first().mutate {
                    it[devotion] = timeOfDay
                }
            )
        }
    }

    fun getPrayerExtraReminderTimeOffsets() =
        notificationsPreferencesDataSource.getPrayerExtraReminderTimeOffsets().map {
            it.toMap()
        }

    fun setPrayerExtraReminderOffset(prayer: Reminder.PrayerExtra, offset: Int) {
        scope.launch {
            notificationsPreferencesDataSource.updatePrayerExtraReminderTimeOffsets(
                notificationsPreferencesDataSource.getPrayerExtraReminderTimeOffsets().first()
                    .mutate {
                        it[prayer] = offset
                    }
            )
        }
    }

    fun getLastNotificationDates() = notificationsPreferencesDataSource.getLastNotificationDates()
        .map { it.toMap() }

    fun setLastNotificationDate(reminder: Reminder, dayOfYear: Int) {
        scope.launch {
            notificationsPreferencesDataSource.updateLastNotificationDates(
                notificationsPreferencesDataSource.getLastNotificationDates().first().mutate {
                    it[reminder] = dayOfYear
                }
            )
        }
    }

}