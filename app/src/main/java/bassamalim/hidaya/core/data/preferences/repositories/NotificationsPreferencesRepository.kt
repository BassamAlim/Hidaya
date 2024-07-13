package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.NotificationsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class NotificationsPreferencesRepository(
    private val dataStore: DataStore<NotificationsPreferences>
) {

    private val flow: Flow<NotificationsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(NotificationsPreferences())
            else throw exception
        }

    suspend fun update(update: (NotificationsPreferences) -> NotificationsPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getNotificationTypes() = flow.map { it.notificationTypes }
    fun getExtraNotificationsMinuteOfDay() = flow.map { it.extraNotificationsMinuteOfDay }
    fun getNotifyExtraNotifications() = flow.map { it.notifyExtraNotifications }
    fun getPrayerReminderOffsets() = flow.map { it.prayerReminderOffsets }
    fun getLastNotificationDates() = flow.map { it.lastNotificationDates }

}