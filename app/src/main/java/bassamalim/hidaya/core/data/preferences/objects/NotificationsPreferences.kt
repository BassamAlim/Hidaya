package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsPreferences(
    val notificationTypes: PersistentMap<PID, NotificationType> = persistentMapOf(),
    val extraNotificationsMinuteOfDay: PersistentMap<PID, Int> = persistentMapOf(),
    val notifyExtraNotifications: PersistentMap<PID, Boolean> = persistentMapOf(),
    val prayerReminderOffsets: PersistentMap<PID, Int> = persistentMapOf(),
    val lastNotificationDates: PersistentMap<PID, Int> = persistentMapOf(),
)