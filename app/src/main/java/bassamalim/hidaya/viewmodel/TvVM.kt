package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.TvRepo
import bassamalim.hidaya.state.TvState
import com.google.android.youtube.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TvVM @Inject constructor(
    repo: TvRepo
): ViewModel() {

    private var ytPlayer: YouTubePlayer? = null
    val apiKey = repo.getApiKey()
    private val quranVidId = repo.getMakkahVidId()
    private val sunnahVidId = repo.getMadinaVidId()

    private val _uiState = MutableStateFlow(TvState())
    val uiState = _uiState.asStateFlow()

    fun onInitializationSuccess(player: YouTubePlayer) {
        ytPlayer = player

        _uiState.update { it.copy(
            isLoading = false
        )}
    }

    fun onQuranChannelClk() {
        ytPlayer?.loadVideo(quranVidId)
        ytPlayer?.play()
    }

    fun onSunnahChannelClk() {
        ytPlayer?.loadVideo(sunnahVidId)
        ytPlayer?.play()
    }

}