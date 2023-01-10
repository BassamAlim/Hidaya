package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.TelawatSuarRepo
import bassamalim.hidaya.state.TelawatSuarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TelawatSuarVM @Inject constructor(
    private val repository: TelawatSuarRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(TelawatSuarState())
    val uiState = _uiState.asStateFlow()

}