package bassamalim.hidaya.features.quizResult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.models.QuizResultQuestion
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizResultVM @Inject constructor(
    private val repository: QuizResultRepo,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val navArgs = savedStateHandle.navArgs<QuizResultNavArgs>()

    private val questions = repository.getQuestions(navArgs.questionIds)

    private val _uiState = MutableStateFlow(QuizResultState(
        score = translateNums(repository.numeralsLanguage, (navArgs.score * 10).toString()),
        questions = getQuestionItems(),
        chosenAs = navArgs.chosenAnswers.toList()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getQuestionItems(): List<QuizResultQuestion> {
        return questions.mapIndexed { i, q ->
            val answers = repository.getAnswers(q.getQuestionId())
            val answersText = List(answers.size) { answers[it].answer_text!! }

            QuizResultQuestion(
                i, questions[i].getQuestionText(), questions[i].getCorrectAnswerId(),
                navArgs.chosenAnswers[i], answersText
            )
        }
    }

}