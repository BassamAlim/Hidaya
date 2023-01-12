package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.LocatorRepo
import bassamalim.hidaya.state.LocatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LocatorVM @Inject constructor(
    private val repository: LocatorRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(LocatorState())
    val uiState = _uiState.asStateFlow()

}