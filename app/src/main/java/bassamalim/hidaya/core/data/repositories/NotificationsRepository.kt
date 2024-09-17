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

    fun getNotificationTypes() = notificationsPreferencesDataSource.getNotificationTypes()

    fun getNotificationType(prayer: Reminder.Prayer) = getNotificationTypes().map { it[prayer]!! }

    suspend fun setNotificationType(type: NotificationType, prayer: Reminder.Prayer) {
        notificationsPreferencesDataSource.getNotificationTypes().first().put(prayer, type)
    }

    fun getDevotionReminderEnabledMap() =
        notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses()

    suspend fun setDevotionReminderEnabled(enabled: Boolean, devotion: Reminder.Devotional) {
        notificationsPreferencesDataSource.updateDevotionalReminderEnabledStatuses(
            notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses().first()
                .put(devotion, enabled)
        )
    }

    fun getDevotionReminderTimes() =
        notificationsPreferencesDataSource.getDevotionalReminderTimes()

    suspend fun setDevotionReminderTimes(timeOfDay: TimeOfDay, devotion: Reminder.Devotional) {
        notificationsPreferencesDataSource.updateDevotionalReminderTimes(
            notificationsPreferencesDataSource.getDevotionalReminderTimes().first()
                .put(devotion, timeOfDay)
        )
    }

    fun getPrayerReminderOffsetMap() =
        notificationsPreferencesDataSource.getPrayerReminderTimeOffsets()

    suspend fun setPrayerReminderOffset(offset: Int, prayer: Reminder.Prayer) {
        notificationsPreferencesDataSource.updatePrayerReminderTimeOffsets(
            notificationsPreferencesDataSource.getPrayerReminderTimeOffsets().first()
                .put(prayer, offset)
        )
    }

    fun getLastNotificationDates() = notificationsPreferencesDataSource.getLastNotificationDates()

    suspend fun setLastNotificationDate(reminder: Reminder, dayOfYear: Int) {
        notificationsPreferencesDataSource.updateLastNotificationDates(
            notificationsPreferencesDataSource.getLastNotificationDates().first()
                .put(reminder, dayOfYear)
        )
    }

}