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
        it.notificationTypeMap[pid]!!
    }

    fun getNotificationTypeMap() = notificationsPreferencesDataSource.flow.map {
        it.notificationTypeMap.toMap()
    }

    fun setNotificationType(type: NotificationType, pid: PID) =
        notificationsPreferencesDataSource.flow.map {
            notificationsPreferencesDataSource.update { it.copy(
                notificationTypeMap = it.notificationTypeMap
                    .put(pid, type)
            )}
        }

    fun getDevotionReminderEnabledMap() = notificationsPreferencesDataSource.flow.map {
        it.devotionReminderEnabledMap.toMap()
    }

    suspend fun setDevotionReminderEnabled(enabled: Boolean, pid: PID) {
        notificationsPreferencesDataSource.update { it.copy(
            devotionReminderEnabledMap = it.devotionReminderEnabledMap
                .put(pid, enabled)
        )}
    }

    fun getDevotionReminderTimeOfDayMap() = notificationsPreferencesDataSource.flow.map {
        it.devotionReminderTimeOfDayMap.toMap()
    }

    suspend fun setDevotionReminderTimeOfDay(timeOfDay: TimeOfDay, pid: PID) {
        notificationsPreferencesDataSource.update { it.copy(
            devotionReminderTimeOfDayMap = it.devotionReminderTimeOfDayMap
                .put(pid, timeOfDay)
        )}
    }

    fun getPrayerReminderOffsetMap() = notificationsPreferencesDataSource.flow.map {
        it.prayerReminderOffsetMap.toMap()
    }

    suspend fun setPrayerReminderOffset(offset: Int, pid: PID) {
        notificationsPreferencesDataSource.update { it.copy(
            prayerReminderOffsetMap = it.prayerReminderOffsetMap
                .put(pid, offset)
        )}
    }

    fun getLastNotificationDateMap() = notificationsPreferencesDataSource.flow.map {
        it.lastNotificationDayOfYearMap.toMap()
    }

    suspend fun setLastNotificationDayOfYear(pid: PID, dayOfYear: Int) {
        notificationsPreferencesDataSource.update { it.copy(
            lastNotificationDayOfYearMap = it.lastNotificationDayOfYearMap
                .put(pid, dayOfYear)
        )}
    }

}