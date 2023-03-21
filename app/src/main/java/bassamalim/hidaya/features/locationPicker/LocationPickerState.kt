package bassamalim.hidaya.features.locationPicker

import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.LocationPickerItem

data class LocationPickerState(
    val titleResId: Int = -1,
    val searchHintResId: Int = R.string.country_hint,
    val items: List<LocationPickerItem> = emptyList()
)