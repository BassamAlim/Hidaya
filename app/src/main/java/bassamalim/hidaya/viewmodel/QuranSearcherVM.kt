package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QuranSearcherRepo
import bassamalim.hidaya.state.QuranSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuranSearcherVM @Inject constructor(
    private val repository: QuranSearcherRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QuranSearcherState())
    val uiState = _uiState.asStateFlow()

}