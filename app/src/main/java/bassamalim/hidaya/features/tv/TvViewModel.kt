package bassamalim.hidaya.features.tv

import androidx.lifecycle.ViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TvViewModel @Inject constructor(
    private val domain: TvDomain
): ViewModel() {

    fun onInitializationSuccess(player: YouTubePlayer) {
        domain.handleInitialization(player)
    }

    fun onQuranChannelClick() {
        domain.playMakkahVideo()
    }

    fun onSunnahChannelClick() {
        domain.playMadinaVideo()
    }

}