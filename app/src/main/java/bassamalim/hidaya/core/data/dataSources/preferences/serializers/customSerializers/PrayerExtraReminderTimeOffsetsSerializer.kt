package bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers

import bassamalim.hidaya.core.enums.Reminder
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
class PrayerExtraReminderTimeOffsetsSerializer(
    private val keySerializer: KSerializer<Reminder.PrayerExtra>,
    private val valueSerializer: KSerializer<Int>
) : KSerializer<PersistentMap<Reminder.PrayerExtra, Int>> {

    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<Reminder.PrayerExtra, Int>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<Reminder.PrayerExtra, Int>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentMap<Reminder.PrayerExtra, Int> {
        return MapSerializer(keySerializer, valueSerializer).deserialize(decoder).toPersistentMap()
    }

}