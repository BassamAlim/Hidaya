package bassamalim.hidaya.core.data.preferences.serializers

import androidx.datastore.core.Serializer
import bassamalim.hidaya.core.data.preferences.dataStore.objects.BooksPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object BooksPreferencesSerializer: Serializer<BooksPreferences> {

    override val defaultValue: BooksPreferences
        get() = BooksPreferences()

    override suspend fun readFrom(input: InputStream): BooksPreferences {
        return try {
            Json.decodeFromString(
                deserializer = BooksPreferences.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: BooksPreferences, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = BooksPreferences.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}