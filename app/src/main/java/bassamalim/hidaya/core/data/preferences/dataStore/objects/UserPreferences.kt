package bassamalim.hidaya.core.data.preferences.dataStore.objects

import bassamalim.hidaya.core.models.Location
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val location: Location? = null,
    val quranPagesRecord: Int = 0,
    val recitationsTimeRecord: Long = 0,
)