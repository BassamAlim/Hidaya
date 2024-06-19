package bassamalim.hidaya.core.data.preferences.serializers

import androidx.datastore.core.Serializer
import bassamalim.hidaya.core.data.preferences.dataStore.objects.NotificationsPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object NotificationsPreferencesSerializer: Serializer<NotificationsPreferences> {

    override val defaultValue: NotificationsPreferences
        get() = NotificationsPreferences()

    override suspend fun readFrom(input: InputStream): NotificationsPreferences {
        return try {
            Json.decodeFromString(
                deserializer = NotificationsPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: NotificationsPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = NotificationsPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}