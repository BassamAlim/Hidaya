package bassamalim.hidaya.core.enums

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LocationType : Parcelable {
    AUTO,
    MANUAL,
    NONE
}