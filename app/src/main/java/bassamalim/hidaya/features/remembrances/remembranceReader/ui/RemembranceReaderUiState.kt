package bassamalim.hidaya.features.remembrances.remembranceReader.ui

data class  RemembranceReaderUiState(
    val title: String = "",
    val textSize: Float = 15f,
    val items: List<RemembrancePassage> = emptyList(),
    val isInfoDialogShown: Boolean = false,
    val infoDialogText: String = ""
)
