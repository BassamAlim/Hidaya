package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.HomeRepo
import bassamalim.hidaya.state.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    private val repository: HomeRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

}