package bassamalim.hidaya.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0
) : Parcelable