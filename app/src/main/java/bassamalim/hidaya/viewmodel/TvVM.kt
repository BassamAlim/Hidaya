package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.WelcomeRepo
import bassamalim.hidaya.state.TvState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TvVM @Inject constructor(
    private val repository: WelcomeRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(TvState())
    val uiState = _uiState.asStateFlow()

}