package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QuranViewerRepo
import bassamalim.hidaya.state.QuranViewerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuranViewerVM @Inject constructor(
    private val repository: QuranViewerRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QuranViewerState())
    val uiState = _uiState.asStateFlow()

}