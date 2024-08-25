package bassamalim.hidaya.core.data.preferences.objects

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class RemembrancesPreferences(
    val favorites: PersistentMap<Int, Int> = persistentMapOf(),
    val textSize: Float = 15f,
)