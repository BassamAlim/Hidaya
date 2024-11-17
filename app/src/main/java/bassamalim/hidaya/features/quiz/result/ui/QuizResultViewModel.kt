package bassamalim.hidaya.features.quiz.result.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuizFullQuestion
import bassamalim.hidaya.core.utils.LangUtils
import bassamalim.hidaya.features.quiz.result.domain.QuizResultDomain
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val domain: QuizResultDomain,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val score = savedStateHandle.get<Int>("score") ?: 0
    private val questions = Gson().fromJson(
        savedStateHandle.get<String>("questions"),
        Array<QuizFullQuestion>::class.java
    )
    private val chosenAnswers = savedStateHandle.get<IntArray>("chosen_answers") ?: intArrayOf()

    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(QuizResultUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuizResultUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            _uiState.update { it.copy(
                isLoading = false,
                questions = questions.mapIndexed { i, q ->
                    QuizResultQuestion(
                        questionNum = i + 1,
                        questionText = q.question,
                        answers = q.answers,
                        chosenAnswerId = chosenAnswers[i]
                    )
                },
                score = LangUtils.translateNums(
                    numeralsLanguage = numeralsLanguage,
                    string = (score * 10).toString()
                )
            )}
        }
    }

}