package bassamalim.hidaya.core.data.preferences.serializers

import androidx.datastore.core.Serializer
import bassamalim.hidaya.core.data.preferences.objects.SupplicationsPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object SupplicationsPreferencesSerializer: Serializer<SupplicationsPreferences> {

    override val defaultValue: SupplicationsPreferences
        get() = SupplicationsPreferences()

    override suspend fun readFrom(input: InputStream): SupplicationsPreferences {
        return try {
            Json.decodeFromString(
                deserializer = SupplicationsPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: SupplicationsPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = SupplicationsPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}