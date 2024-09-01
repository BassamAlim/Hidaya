package bassamalim.hidaya.core.models

import android.os.Parcelable
import bassamalim.hidaya.core.enums.LocationType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Location(
    val type: LocationType,
    val coordinates: Coordinates,
    val ids: LocationIds
) : Parcelable