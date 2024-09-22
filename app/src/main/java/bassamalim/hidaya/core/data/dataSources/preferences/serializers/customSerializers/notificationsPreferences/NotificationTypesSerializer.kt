package bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.notificationsPreferences

import bassamalim.hidaya.core.enums.NotificationType
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
class NotificationTypesSerializer(
    private val keySerializer: KSerializer<Reminder.Prayer>,
    private val valueSerializer: KSerializer<NotificationType>
) : KSerializer<PersistentMap<Reminder.Prayer, NotificationType>> {

    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<Reminder.Prayer, NotificationType>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<Reminder.Prayer, NotificationType>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentMap<Reminder.Prayer, NotificationType> {
        return MapSerializer(keySerializer, valueSerializer).deserialize(decoder).toPersistentMap()
    }

}