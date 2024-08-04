package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val notificationsPreferencesDataSource: NotificationsPreferencesDataSource
) {

    fun getNotificationType(pid: PID) = notificationsPreferencesDataSource.flow.map {
        it.notificationTypes[pid]!!
    }

    fun getNotificationTypes() = notificationsPreferencesDataSource.flow.map {
        it.notificationTypes.toMap()
    }

    suspend fun setNotificationTypes(notificationTypes: Map<PID, NotificationType>) {
        notificationsPreferencesDataSource.update { it.copy(
            notificationTypes = notificationTypes.toPersistentMap()
        )}
    }

    fun getExtraNotificationsMinuteOfDay() = notificationsPreferencesDataSource.flow.map {
        it.extraNotificationsMinuteOfDay.toMap()
    }

    suspend fun setExtraNotificationsMinuteOfDay(minuteOfDay: Map<PID, Int>) {
        notificationsPreferencesDataSource.update { it.copy(
            extraNotificationsMinuteOfDay = minuteOfDay.toPersistentMap()
        )}
    }

    fun getNotifyExtraNotifications() = notificationsPreferencesDataSource.flow.map {
        it.notifyExtraNotifications.toMap()
    }

    suspend fun setNotifyExtraNotifications(notify: Map<PID, Boolean>) {
        notificationsPreferencesDataSource.update { it.copy(
            notifyExtraNotifications = notify.toPersistentMap()
        )}
    }

    fun getPrayerReminderOffsets() = notificationsPreferencesDataSource.flow.map {
        it.prayerReminderOffsets.toMap()
    }

    suspend fun setPrayerReminderOffsets(offsets: Map<PID, Int>) {
        notificationsPreferencesDataSource.update { it.copy(
            prayerReminderOffsets = offsets.toPersistentMap()
        )}
    }

    fun getLastNotificationDates() = notificationsPreferencesDataSource.flow.map {
        it.lastNotificationDates.toMap()
    }

    suspend fun setLastNotificationDates(dates: Map<PID, Int>) {
        notificationsPreferencesDataSource.update { it.copy(
            lastNotificationDates = dates.toPersistentMap()
        )}
    }

}