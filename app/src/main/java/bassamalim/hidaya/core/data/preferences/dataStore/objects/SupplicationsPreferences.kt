package bassamalim.hidaya.core.data.preferences.dataStore.objects

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class SupplicationsPreferences(
    val textSize: Float = 15f,
    val favorites: PersistentMap<Int, Int> = persistentMapOf(),
)