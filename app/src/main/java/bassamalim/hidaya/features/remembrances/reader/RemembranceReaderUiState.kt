package bassamalim.hidaya.features.remembrances.reader

data class  RemembranceReaderUiState(
    val title: String = "",
    val textSize: Float = 15f,
    val items: List<RemembrancePassage> = emptyList(),
)
