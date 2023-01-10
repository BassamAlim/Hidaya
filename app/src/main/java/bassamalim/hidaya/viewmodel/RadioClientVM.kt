package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.RadioClientRepo
import bassamalim.hidaya.state.RadioClientState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RadioClientVM @Inject constructor(
    private val repository: RadioClientRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(RadioClientState())
    val uiState = _uiState.asStateFlow()

}