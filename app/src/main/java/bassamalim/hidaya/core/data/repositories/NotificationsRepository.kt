package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val notificationsPreferencesDataSource: NotificationsPreferencesDataSource
) {

    fun getNotificationTypes() = notificationsPreferencesDataSource.getNotificationTypes().map {
        it.toMap()
    }

    fun getNotificationType(prayer: Reminder.Prayer) = getNotificationTypes().map { it[prayer]!! }

    suspend fun setNotificationType(type: NotificationType, prayer: Reminder.Prayer) {
        notificationsPreferencesDataSource.getNotificationTypes().first().put(prayer, type)
    }

    fun getDevotionalReminderEnabledMap() =
        notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses().map {
            it.toMap()
        }

    suspend fun setDevotionalReminderEnabled(enabled: Boolean, devotion: Reminder.Devotional) {
        notificationsPreferencesDataSource.updateDevotionalReminderEnabledStatuses(
            notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses().first()
                .put(devotion, enabled)
        )
    }

    fun getDevotionalReminderTimes() =
        notificationsPreferencesDataSource.getDevotionalReminderTimes().map {
            it.toMap()
        }

    suspend fun setDevotionalReminderTimes(timeOfDay: TimeOfDay, devotion: Reminder.Devotional) {
        notificationsPreferencesDataSource.updateDevotionalReminderTimes(
            notificationsPreferencesDataSource.getDevotionalReminderTimes().first()
                .put(devotion, timeOfDay)
        )
    }

    fun getPrayerExtraReminderTimeOffsets() =
        notificationsPreferencesDataSource.getPrayerExtraReminderTimeOffsets().map {
            it.toMap()
        }

    suspend fun setPrayerExtraReminderOffset(offset: Int, prayer: Reminder.Prayer) {
        notificationsPreferencesDataSource.updatePrayerExtraReminderTimeOffsets(
            notificationsPreferencesDataSource.getPrayerExtraReminderTimeOffsets().first()
                .put(prayer, offset)
        )
    }

    fun getLastNotificationDates() = notificationsPreferencesDataSource.getLastNotificationDates()
        .map { it.toMap() }

    suspend fun setLastNotificationDate(reminder: Reminder, dayOfYear: Int) {
        notificationsPreferencesDataSource.updateLastNotificationDates(
            notificationsPreferencesDataSource.getLastNotificationDates().first()
                .put(reminder, dayOfYear)
        )
    }

}