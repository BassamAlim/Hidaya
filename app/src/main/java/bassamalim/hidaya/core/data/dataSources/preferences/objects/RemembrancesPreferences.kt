package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.IntBooleanPersistentMapSerializer
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class RemembrancesPreferences(
    @Serializable(with = IntBooleanPersistentMapSerializer::class)
    val favorites: PersistentMap<Int, Boolean> = persistentMapOf(),
    val textSize: Float = 15f,
)