package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.repository.PrayersRepo
import bassamalim.hidaya.state.PrayersState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PrayersVM @Inject constructor(
    private val repository: PrayersRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(PrayersState())
    val uiState = _uiState.asStateFlow()

}