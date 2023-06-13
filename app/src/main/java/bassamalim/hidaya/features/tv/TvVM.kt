package bassamalim.hidaya.features.tv

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

@HiltViewModel
class TvVM @Inject constructor(
    repo: TvRepo
): ViewModel() {

    private var ytPlayer: YouTubePlayer? = null
    private val quranVidId = repo.getMakkahVidId()
    private val sunnahVidId = repo.getMadinaVidId()

    fun onInitializationSuccess(player: YouTubePlayer) {
        ytPlayer = player
    }

    fun onQuranChannelClk() {
        ytPlayer?.loadVideo(quranVidId, 0f)
        ytPlayer?.play()
    }

    fun onSunnahChannelClk() {
        ytPlayer?.loadVideo(sunnahVidId, 0f)
        ytPlayer?.play()
    }

}