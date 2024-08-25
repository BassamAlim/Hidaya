package bassamalim.hidaya.features.quiz.quizResult.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quiz.quizResult.domain.QuizResultDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow(
        QuizResultUiState(
        score = translateNums(
            numeralsLanguage = numeralsLanguage,
            string = (score * 10).toString()
        ),
        questions = getQuestionItems()
    )
    )
    val uiState = _uiState.asStateFlow()

    private fun getQuestionItems(): List<QuizResultQuestion> {
        val questions = domain.getQuestions(questionIds)
        return questions.mapIndexed { i, q ->
            val answers = domain.getAnswers(q.id)
            val answersText = List(answers.size) { answers[it].text }

            QuizResultQuestion(
                i, questions[i].text!!, questions[i].correctAnswerId,
                chosenAnswers[i], answersText
            )
        }
    }

}