package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.QuranRepo
import bassamalim.hidaya.state.QuranState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuranVM @Inject constructor(
    private val repository: QuranRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(QuranState())
    val uiState = _uiState.asStateFlow()

}