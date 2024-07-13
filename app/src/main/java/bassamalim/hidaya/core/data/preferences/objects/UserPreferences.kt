package bassamalim.hidaya.core.data.preferences.objects

import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.UserRecord
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val location: Location? = null,
    val userRecord: UserRecord = UserRecord(),
)