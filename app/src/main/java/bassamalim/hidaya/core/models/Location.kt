package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Location {

    @Serializable
    data class FetchedLocation(
        val latitude: Double,
        val longitude: Double,
    ) : Location()

    @Serializable
    data class SelectedLocation(
        val countryId: Int,
        val cityId: Int,
    ) : Location()

    @Serializable
    data class NoLocation(
        val message: String,
    ) : Location()

}