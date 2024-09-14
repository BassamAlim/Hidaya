package bassamalim.hidaya.features.quiz.result.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuizFullQuestion
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quiz.result.domain.QuizResultDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val domain: QuizResultDomain,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val score = savedStateHandle.get<Int>("score") ?: 0
    private val questionIds = savedStateHandle.get<IntArray>("questions") ?: intArrayOf()
    private val chosenAnswers = savedStateHandle.get<IntArray>("chosen_answers") ?: intArrayOf()

    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(QuizResultUiState(
        score = translateNums(
            numeralsLanguage = numeralsLanguage,
            string = (score * 10).toString()
        )
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(
                questions = getQuestionItems(domain.getFullQuestions(questionIds))
            )}
        }
    }

    private fun getQuestionItems(fullQuestions: List<QuizFullQuestion>): List<QuizResultQuestion> {
        return fullQuestions.mapIndexed { i, q ->
            QuizResultQuestion(
                questionNum = i + 1,
                questionText = q.question,
                answers = q.answers,
                correctAnswerId = q.correctAnswerId,
                chosenAnswerId = chosenAnswers[i]
            )
        }
    }

}