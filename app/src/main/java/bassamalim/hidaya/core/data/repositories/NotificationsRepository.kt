package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val notificationsPreferencesDataSource: NotificationsPreferencesDataSource
) {

    fun getNotificationTypes() = notificationsPreferencesDataSource.getNotificationTypes()

    fun getNotificationType(pid: PID) = getNotificationTypes().map { it[pid]!! }

    suspend fun setNotificationType(type: NotificationType, pid: PID) {
        notificationsPreferencesDataSource.getNotificationTypes().first().put(pid, type)
    }

    fun getDevotionReminderEnabledMap() =
        notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses()

    suspend fun setDevotionReminderEnabled(enabled: Boolean, pid: PID) {
        notificationsPreferencesDataSource.updateDevotionalReminderEnabledStatuses(
            notificationsPreferencesDataSource.getDevotionalReminderEnabledStatuses().first()
                .put(pid, enabled)
        )
    }

    fun getDevotionReminderTimes() =
        notificationsPreferencesDataSource.getDevotionalReminderTimes()

    suspend fun setDevotionReminderTimes(timeOfDay: TimeOfDay, pid: PID) {
        notificationsPreferencesDataSource.updateDevotionalReminderTimes(
            notificationsPreferencesDataSource.getDevotionalReminderTimes().first()
                .put(pid, timeOfDay)
        )
    }

    fun getPrayerReminderOffsetMap() =
        notificationsPreferencesDataSource.getPrayerReminderTimeOffsets()

    suspend fun setPrayerReminderOffset(offset: Int, pid: PID) {
        notificationsPreferencesDataSource.updatePrayerReminderTimeOffsets(
            notificationsPreferencesDataSource.getPrayerReminderTimeOffsets().first()
                .put(pid, offset)
        )
    }

    fun getLastNotificationDates() = notificationsPreferencesDataSource.getLastNotificationDates()

    suspend fun setLastNotificationDate(pid: PID, dayOfYear: Int) {
        notificationsPreferencesDataSource.updateLastNotificationDates(
            notificationsPreferencesDataSource.getLastNotificationDates().first()
                .put(pid, dayOfYear)
        )
    }

}