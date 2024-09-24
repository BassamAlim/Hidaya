package bassamalim.hidaya.features.tv.domain

import android.util.Log
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import bassamalim.hidaya.core.other.Global
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TvDomain @Inject constructor(
    liveContentRepository: LiveContentRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private val quranVidId = liveContentRepository.getMakkahVideoId()
    private val sunnahVidId = liveContentRepository.getMadinaVideoId()
    private var ytPlayer: YouTubePlayer? = null

    fun handleInitialization(player: YouTubePlayer) {
        Log.i(
            Global.TAG,
            "Youtube player initialized - " +
                    "Quran video id: $quranVidId, " +
                    "Sunnah video id: $sunnahVidId"
        )

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

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}