package bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers

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
class StringBooleanPersistentMapSerializer(
    private val keySerializer: KSerializer<String>,
    private val valueSerializer: KSerializer<Boolean>
) : KSerializer<PersistentMap<String, Boolean>> {

    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<String, Boolean>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<String, Boolean>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentMap<String, Boolean> {
        return MapSerializer(keySerializer, valueSerializer).deserialize(decoder).toPersistentMap()
    }

}