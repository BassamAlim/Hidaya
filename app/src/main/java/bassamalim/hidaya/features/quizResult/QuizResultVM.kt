package bassamalim.hidaya.features.quizResult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.models.QuizResultQuestion
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizResultVM @Inject constructor(
    private val repository: QuizResultRepo,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val score = savedStateHandle.get<Int>("score") ?: 0
    private val questionIds = savedStateHandle.get<IntArray>("questions") ?: intArrayOf()
    private val chosenAs = savedStateHandle.get<IntArray>("chosenAs") ?: IntArray(10)

    private val questions = repository.getQuestions(questionIds)

    private val _uiState = MutableStateFlow(
        QuizResultState(
        score = translateNums(repository.numeralsLanguage, (score * 10).toString()),
        questions = getQuestionItems(),
        chosenAs = chosenAs.toList()
    )
    )
    val uiState = _uiState.asStateFlow()

    private fun getQuestionItems(): List<QuizResultQuestion> {
        return questions.mapIndexed { i, q ->
            val answers = repository.getAnswers(q.getQuestionId())
            val answersText = List(answers.size) { answers[it].answer_text!! }

            QuizResultQuestion(
                i, questions[i].getQuestionText(), questions[i].getCorrectAnswerId(),
                chosenAs[i], answersText
            )
        }
    }

}