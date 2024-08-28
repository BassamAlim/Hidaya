package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsPreferences(
    val notificationTypeMap: PersistentMap<PID, NotificationType> = persistentMapOf(),
    val devotionReminderEnabledMap: PersistentMap<PID, Boolean> = persistentMapOf(),
    val devotionReminderTimeOfDayMap: PersistentMap<PID, TimeOfDay> = persistentMapOf(),
    val prayerReminderOffsetMap: PersistentMap<PID, Int> = persistentMapOf(),
    val lastNotificationDayOfYearMap: PersistentMap<PID, Int> = persistentMapOf(),
)