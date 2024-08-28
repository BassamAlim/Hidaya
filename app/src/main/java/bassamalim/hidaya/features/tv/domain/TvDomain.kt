package bassamalim.hidaya.features.tv.domain

import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import javax.inject.Inject

class TvDomain @Inject constructor(
    liveContentRepository: LiveContentRepository
) {

    private val quranVidId = liveContentRepository.getMakkahVideoId()
    private val sunnahVidId = liveContentRepository.getMadinaVideoId()
    private var ytPlayer: YouTubePlayer? = null

    fun handleInitialization(player: YouTubePlayer) {
        ytPlayer = player
        ytPlayer?.loadVideo(quranVidId, 0f)
    }

    fun playMakkahVideo() {
        ytPlayer?.loadVideo(quranVidId, 0f)
        ytPlayer?.play()
    }

    fun playMadinaVideo() {
        ytPlayer?.loadVideo(sunnahVidId, 0f)
        ytPlayer?.play()
    }

}