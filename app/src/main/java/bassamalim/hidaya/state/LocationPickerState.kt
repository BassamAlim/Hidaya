package bassamalim.hidaya.state

import bassamalim.hidaya.models.LocationPickerItem

data class LocationPickerState(
    val titleResId: Int = -1,
    val items: List<LocationPickerItem> = emptyList()
)