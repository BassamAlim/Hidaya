package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.DevotionalReminderEnabledStatusesSerializer
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.DevotionalReminderTimesSerializer
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.LastNotificationDatesSerializer
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.NotificationTypesSerializer
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.PrayerExtraReminderTimeOffsetsSerializer
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.models.TimeOfDay
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsPreferences(
    @Serializable(with = NotificationTypesSerializer::class)
    val notificationTypes: PersistentMap<Reminder.Prayer, NotificationType> = persistentMapOf(
        Reminder.Prayer.Fajr to NotificationType.NOTIFICATION,
        Reminder.Prayer.Sunrise to NotificationType.NONE,
        Reminder.Prayer.Dhuhr to NotificationType.NOTIFICATION,
        Reminder.Prayer.Asr to NotificationType.NOTIFICATION,
        Reminder.Prayer.Maghrib to NotificationType.NOTIFICATION,
        Reminder.Prayer.Ishaa to NotificationType.NOTIFICATION
    ),
    @Serializable(with = DevotionalReminderEnabledStatusesSerializer::class)
    val devotionalReminderEnabledStatuses: PersistentMap<Reminder.Devotional, Boolean> = persistentMapOf(
        Reminder.Devotional.MorningRemembrances to true,
        Reminder.Devotional.EveningRemembrances to true,
        Reminder.Devotional.DailyWerd to true,
        Reminder.Devotional.FridayKahf to true
    ),
    @Serializable(with = DevotionalReminderTimesSerializer::class)
    val devotionalReminderTimes: PersistentMap<Reminder.Devotional, TimeOfDay> = persistentMapOf(
        Reminder.Devotional.MorningRemembrances to TimeOfDay(5, 0),
        Reminder.Devotional.EveningRemembrances to TimeOfDay(18, 0),
        Reminder.Devotional.DailyWerd to TimeOfDay(21, 0),
        Reminder.Devotional.FridayKahf to TimeOfDay(10, 0)
    ),
    @Serializable(with = PrayerExtraReminderTimeOffsetsSerializer::class)
    val prayerExtraReminderTimeOffsets: PersistentMap<Reminder.PrayerExtra, Int> = persistentMapOf(
        Reminder.PrayerExtra.Fajr to 0,
        Reminder.PrayerExtra.Sunrise to 0,
        Reminder.PrayerExtra.Dhuhr to 0,
        Reminder.PrayerExtra.Asr to 0,
        Reminder.PrayerExtra.Maghrib to 0,
        Reminder.PrayerExtra.Ishaa to 0
    ),
    @Serializable(with = LastNotificationDatesSerializer::class)
    val lastNotificationDates: PersistentMap<Reminder, Int> = persistentMapOf(
        Reminder.Prayer.Fajr to 0,
        Reminder.Prayer.Sunrise to 0,
        Reminder.Prayer.Dhuhr to 0,
        Reminder.Prayer.Asr to 0,
        Reminder.Prayer.Maghrib to 0,
        Reminder.Prayer.Ishaa to 0,
        Reminder.Devotional.MorningRemembrances to 0,
        Reminder.Devotional.EveningRemembrances to 0,
        Reminder.Devotional.DailyWerd to 0,
        Reminder.Devotional.FridayKahf to 0
    ),
)