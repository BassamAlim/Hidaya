package bassamalim.hidaya.features.recitations.recitersMenuFilter.ui

data class RecitersMenuFilterUiState(
    val isLoading: Boolean = true,
    val options: Map<String, Boolean> = emptyMap()
)
