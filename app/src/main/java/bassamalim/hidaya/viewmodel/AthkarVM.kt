package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.AthkarRepo
import bassamalim.hidaya.state.AthkarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AthkarVM @Inject constructor(
    private val repository: AthkarRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(AthkarState())
    val uiState = _uiState.asStateFlow()

}