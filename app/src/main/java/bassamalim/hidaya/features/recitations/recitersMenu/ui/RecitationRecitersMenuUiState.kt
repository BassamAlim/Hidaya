package bassamalim.hidaya.features.recitations.recitersMenu.ui

import bassamalim.hidaya.features.quran.surasMenu.ui.LastPlayedMedia

data class RecitationRecitersMenuUiState(
    val isLoading: Boolean = true,
    val lastPlayedMedia: LastPlayedMedia? = null,
    val searchText: String = "",
    val isFiltered: Boolean = false
)