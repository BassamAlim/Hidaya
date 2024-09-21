package bassamalim.hidaya.features.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.tv.domain.TvDomain
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvViewModel @Inject constructor(
    private val domain: TvDomain
): ViewModel() {

    lateinit var language: Language

    init {
        viewModelScope.launch {
            language = domain.getLanguage().first()
        }
    }

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