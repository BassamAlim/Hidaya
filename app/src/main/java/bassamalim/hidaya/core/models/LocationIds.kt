package bassamalim.hidaya.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LocationIds(
    val countryId: Int,
    val cityId: Int
) : Parcelable
