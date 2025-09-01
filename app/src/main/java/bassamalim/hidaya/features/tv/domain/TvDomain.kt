package bassamalim.hidaya.features.tv.domain

import android.util.Log
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.data.repositories.AnalyticsRepository
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import bassamalim.hidaya.core.models.AnalyticsEvent
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TvDomain @Inject constructor(
    liveContentRepository: LiveContentRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val analyticsRepository: AnalyticsRepository
) {

    private val quranVidId = liveContentRepository.getMakkahVideoId()
    private val sunnahVidId = liveContentRepository.getMadinaVideoId()
    private var ytPlayer: YouTubePlayer? = null

    fun handleInitialization(player: YouTubePlayer) {
        Log.i(
            Globals.TAG,
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

        trackTvChannelViewed("Quran Channel")
    }

    fun playMadinaVideo() {
        ytPlayer?.loadVideo(sunnahVidId, 0f)
        ytPlayer?.play()

        trackTvChannelViewed("Sunnah Channel")
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    private fun trackTvChannelViewed(channel: String) {
        analyticsRepository.trackEvent(AnalyticsEvent.TvChannelViewed(channel))
    }

}