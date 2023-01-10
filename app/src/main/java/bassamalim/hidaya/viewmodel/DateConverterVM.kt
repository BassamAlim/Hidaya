package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.DateConverterRepo
import bassamalim.hidaya.state.DateConverterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DateConverterVM @Inject constructor(
    private val repository: DateConverterRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(DateConverterState())
    val uiState = _uiState.asStateFlow()

}