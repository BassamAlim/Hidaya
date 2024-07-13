package bassamalim.hidaya.core.data.preferences.serializers

import androidx.datastore.core.Serializer
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object QuranPreferencesSerializer: Serializer<QuranPreferences> {

    override val defaultValue: QuranPreferences
        get() = QuranPreferences()

    override suspend fun readFrom(input: InputStream): QuranPreferences {
        return try {
            Json.decodeFromString(
                deserializer = QuranPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: QuranPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = QuranPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}