package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsPreferences(
    val notificationTypes: PersistentMap<PID, NotificationType> = persistentMapOf(),
    val devotionalReminderEnabled: PersistentMap<PID, Boolean> = persistentMapOf(),
    val devotionalReminderTimes: PersistentMap<PID, TimeOfDay> = persistentMapOf(),
    val prayerReminderTimeOffsets: PersistentMap<PID, Int> = persistentMapOf(),
    val lastNotificationDates: PersistentMap<PID, Int> = persistentMapOf(),
)