package bassamalim.hidaya.core.data.preferences.dataStore.serializers

import androidx.datastore.core.Serializer
import bassamalim.hidaya.core.data.preferences.dataStore.objects.PrayersPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object PrayersPreferencesSerializer: Serializer<PrayersPreferences> {

    override val defaultValue: PrayersPreferences
        get() = PrayersPreferences()

    override suspend fun readFrom(input: InputStream): PrayersPreferences {
        return try {
            Json.decodeFromString(
                deserializer = PrayersPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: PrayersPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = PrayersPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}