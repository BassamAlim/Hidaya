package bassamalim.hidaya.features.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.tv.domain.TvDomain
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvViewModel @Inject constructor(
    private val domain: TvDomain
): ViewModel() {

    lateinit var language: Language

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = TvUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()

            _uiState.update { it.copy(
                isLoading = false
            )}
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