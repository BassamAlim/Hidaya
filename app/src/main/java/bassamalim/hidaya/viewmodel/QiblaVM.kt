package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QiblaRepo
import bassamalim.hidaya.state.QiblaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QiblaVM @Inject constructor(
    private val repository: QiblaRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QiblaState())
    val uiState = _uiState.asStateFlow()

}