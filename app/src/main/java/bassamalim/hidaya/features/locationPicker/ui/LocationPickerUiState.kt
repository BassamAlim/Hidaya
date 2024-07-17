package bassamalim.hidaya.features.locationPicker.ui

data class LocationPickerUiState(
    val mode: LocationPickerMode = LocationPickerMode.COUNTRY,
    val searchText: String = "",
    val items: List<LocationPickerItem> = emptyList()
)