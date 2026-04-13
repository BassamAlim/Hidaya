package bassamalim.hidaya.features.recitations.recitersMenuFilter

data class RecitersMenuFilterUiState(
    val isLoading: Boolean = true,
    val options: Map<String, Boolean> = emptyMap()
)
