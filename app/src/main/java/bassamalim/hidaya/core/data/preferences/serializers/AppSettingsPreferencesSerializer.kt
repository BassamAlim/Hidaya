package bassamalim.hidaya.core.data.preferences.serializers

import androidx.datastore.core.Serializer
import bassamalim.hidaya.core.data.preferences.dataStore.objects.AppSettingsPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppSettingsPreferencesSerializer: Serializer<AppSettingsPreferences> {

    override val defaultValue: AppSettingsPreferences
        get() = AppSettingsPreferences()

    override suspend fun readFrom(input: InputStream): AppSettingsPreferences {
        return try {
            Json.decodeFromString(
                deserializer = AppSettingsPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: AppSettingsPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = AppSettingsPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}