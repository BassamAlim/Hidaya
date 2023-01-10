package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.TelawatRepo
import bassamalim.hidaya.state.TelawatState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TelawatVM @Inject constructor(
    private val repository: TelawatRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(TelawatState())
    val uiState = _uiState.asStateFlow()

}