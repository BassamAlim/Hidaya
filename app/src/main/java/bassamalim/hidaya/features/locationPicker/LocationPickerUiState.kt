package bassamalim.hidaya.features.locationPicker

data class LocationPickerUiState(
    val mode: LocationPickerMode = LocationPickerMode.COUNTRY,
    val searchText: String = "",
    val items: List<LocationPickerItem> = emptyList()
)