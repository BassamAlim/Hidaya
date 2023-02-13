package bassamalim.hidaya.state

import bassamalim.hidaya.R
import bassamalim.hidaya.models.LocationPickerItem

data class LocationPickerState(
    val titleResId: Int = -1,
    val searchHintResId: Int = R.string.country_hint,
    val items: List<LocationPickerItem> = emptyList()
)