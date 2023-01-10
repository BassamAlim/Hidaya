package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.TelawatClientRepo
import bassamalim.hidaya.state.TelawatClientState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TelawatClientVM @Inject constructor(
    private val repository: TelawatClientRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(TelawatClientState())
    val uiState = _uiState.asStateFlow()

}