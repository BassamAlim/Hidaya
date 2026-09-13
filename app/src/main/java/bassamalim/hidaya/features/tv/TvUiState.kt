package bassamalim.hidaya.features.tv

data class TvUiState(
    val selectedChannel: TvChannel = TvChannel.QURAN,
    val isLoading: Boolean = true,
    val isPlaybackFailed: Boolean = false,
    val isFullscreen: Boolean = false
)
