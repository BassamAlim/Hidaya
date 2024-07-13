package bassamalim.hidaya.features.locationPicker

import bassamalim.hidaya.R

data class LocationPickerState(
    val titleResId: Int = -1,
    val searchHintResId: Int = R.string.country_hint,
    val items: List<LocationPickerItem> = emptyList()
)