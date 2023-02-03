package bassamalim.hidaya.viewmodel

import android.app.Activity
import android.app.Application
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.repository.TvRepo
import com.google.android.youtube.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TvVM @Inject constructor(
    app: Application,
    repository: TvRepo
): AndroidViewModel(app) {

    var apiKey = ""
    private var ytPlayer: YouTubePlayer? = null
    private var quranVidId = ""
    private var sunnahVidId = ""

    init {
        (app.applicationContext as Activity).window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        repository.fetchInfo { apiKey, quranVidId, sunnahVidId ->
            onInfoFetchSuccess(apiKey, quranVidId, sunnahVidId)
        }
    }

    private fun onInfoFetchSuccess(apiKey: String, quranVidId: String, sunnahVidId: String) {
        this.apiKey = apiKey
        this.quranVidId = quranVidId
        this.sunnahVidId = sunnahVidId
    }

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