package bassamalim.hidaya.core.data.dataSources.preferences.serializers

import kotlinx.serialization.json.Json

val json = Json {
    allowStructuredMapKeys = true  // Allows serializing maps with sealed class keys
}
