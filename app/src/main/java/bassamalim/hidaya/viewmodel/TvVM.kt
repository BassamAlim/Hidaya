package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.TvRepo
import com.google.android.youtube.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TvVM @Inject constructor(
    repo: TvRepo
): ViewModel() {

    private var ytPlayer: YouTubePlayer? = null
    val apiKey = repo.getApiKey()
    private val quranVidId = repo.getMakkahVidId()
    private val sunnahVidId = repo.getMadinaVidId()

    fun onInitializationSuccess(player: YouTubePlayer) {
        ytPlayer = player
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