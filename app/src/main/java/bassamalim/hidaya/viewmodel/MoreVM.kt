package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.HomeRepo
import bassamalim.hidaya.repository.MainRepo
import bassamalim.hidaya.repository.MoreRepo
import bassamalim.hidaya.state.MainState
import bassamalim.hidaya.state.MoreState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MoreVM @Inject constructor(
    private val repository: MoreRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(MoreState())
    val uiState = _uiState.asStateFlow()

}