package bassamalim.hidaya.core.models

import bassamalim.hidaya.core.enums.LocationType
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val type: LocationType,
    val latitude: Double,
    val longitude: Double,
    val countryId: Int,
    val cityId: Int,
)