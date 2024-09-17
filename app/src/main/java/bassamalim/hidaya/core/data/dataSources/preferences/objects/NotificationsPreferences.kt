package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsPreferences(
    val notificationTypes: PersistentMap<Reminder.Prayer, NotificationType> = persistentMapOf(),
    val devotionalReminderEnabledStatuses: PersistentMap<Reminder.Devotional, Boolean> = persistentMapOf(),
    val devotionalReminderTimes: PersistentMap<Reminder.Devotional, TimeOfDay> = persistentMapOf(),
    val prayerReminderTimeOffsets: PersistentMap<Reminder.Prayer, Int> = persistentMapOf(),
    val lastNotificationDates: PersistentMap<Reminder, Int> = persistentMapOf(),
)