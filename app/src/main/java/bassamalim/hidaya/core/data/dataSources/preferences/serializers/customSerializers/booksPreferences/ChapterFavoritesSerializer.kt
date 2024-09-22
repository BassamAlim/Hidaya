package bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.booksPreferences

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class ChapterFavoritesSerializer(
    private val keySerializer: KSerializer<Int>,
    private val valueSerializer: KSerializer<PersistentMap<Int, Boolean>>
) : KSerializer<PersistentMap<Int, PersistentMap<Int, Boolean>>> {

    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<Int, Map<Int, Boolean>>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<Int, PersistentMap<Int, Boolean>>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentMap<Int, PersistentMap<Int, Boolean>> {
        return MapSerializer(keySerializer, valueSerializer).deserialize(decoder).toPersistentMap()
    }

}