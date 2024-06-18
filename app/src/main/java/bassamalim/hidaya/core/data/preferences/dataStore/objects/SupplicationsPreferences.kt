package bassamalim.hidaya.core.data.preferences.dataStore.objects

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class SupplicationsPreferences(
    val textSize: Float = 15f,
    val favoriteIds: PersistentList<Int> = persistentListOf(),
)