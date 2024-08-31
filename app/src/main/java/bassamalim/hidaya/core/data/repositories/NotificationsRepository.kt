package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val notificationsPreferencesDataSource: NotificationsPreferencesDataSource
) {

    fun getNotificationType(pid: PID) = notificationsPreferencesDataSource.flow.map {
        it.notificationTypes[pid]!!
    }

    fun getNotificationTypeMap() = notificationsPreferencesDataSource.flow.map {
        it.notificationTypes.toMap()
    }

    fun setNotificationType(type: NotificationType, pid: PID) =
        notificationsPreferencesDataSource.flow.map {
            notificationsPreferencesDataSource.update { it.copy(
                notificationTypes = it.notificationTypes
                    .put(pid, type)
            )}
        }

    fun getDevotionReminderEnabledMap() = notificationsPreferencesDataSource.flow.map {
        it.devotionalReminderEnabled.toMap()
    }

    suspend fun setDevotionReminderEnabled(enabled: Boolean, pid: PID) {
        notificationsPreferencesDataSource.update { it.copy(
            devotionalReminderEnabled = it.devotionalReminderEnabled
                .put(pid, enabled)
        )}
    }

    fun getDevotionReminderTimeOfDayMap() = notificationsPreferencesDataSource.flow.map {
        it.devotionalReminderTimes.toMap()
    }

    suspend fun setDevotionReminderTimeOfDay(timeOfDay: TimeOfDay, pid: PID) {
        notificationsPreferencesDataSource.update { it.copy(
            devotionalReminderTimes = it.devotionalReminderTimes
                .put(pid, timeOfDay)
        )}
    }

    fun getPrayerReminderOffsetMap() = notificationsPreferencesDataSource.flow.map {
        it.prayerReminderTimeOffsets.toMap()
    }

    suspend fun setPrayerReminderOffset(offset: Int, pid: PID) {
        notificationsPreferencesDataSource.update { it.copy(
            prayerReminderTimeOffsets = it.prayerReminderTimeOffsets
                .put(pid, offset)
        )}
    }

    fun getLastNotificationDateMap() = notificationsPreferencesDataSource.flow.map {
        it.lastNotificationDates.toMap()
    }

    suspend fun setLastNotificationDayOfYear(pid: PID, dayOfYear: Int) {
        notificationsPreferencesDataSource.update { it.copy(
            lastNotificationDates = it.lastNotificationDates
                .put(pid, dayOfYear)
        )}
    }

}