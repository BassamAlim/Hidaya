package bassamalim.hidaya.features.recitations.recitersMenuFilter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.recitations.recitersMenuFilter.domain.RecitersMenuFilterDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecitersMenuFilterViewModel @Inject constructor(
    private val domain: RecitersMenuFilterDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language

    private val _uiState = MutableStateFlow(RecitersMenuFilterUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecitersMenuFilterUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()

            _uiState.update { it.copy(
                isLoading = false,
                options = domain.getOptions(language)
            )}
        }
    }

    fun onSelection(name: String, isSelected: Boolean) {
        _uiState.update { it.copy(
            options = it.options.toMutableMap().apply {
                this[name] = isSelected
            }
        )}
    }

    fun onSelectAll() {
        _uiState.update { it.copy(
            options = it.options.mapValues { true }
        )}
    }

    fun onUnselectAll() {
        _uiState.update { it.copy(
            options = it.options.mapValues { false }
        )}
    }

    fun onDismiss() {
        navigator.popBackStack()
    }

    fun onSave() {
        viewModelScope.launch {
            domain.setOptions(_uiState.value.options)

            navigator.popBackStack()
        }
    }

}