package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.WelcomeRepo
import bassamalim.hidaya.state.WelcomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WelcomeVM @Inject constructor(
    private val repository: WelcomeRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeState())
    val uiState = _uiState.asStateFlow()

}