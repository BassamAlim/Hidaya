package bassamalim.hidaya.features.quiz

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Arrays
import javax.inject.Inject

@HiltViewModel
class QuizVM @Inject constructor(
    private val repo: QuizRepo,
    private val navigator: Navigator
): ViewModel() {

    private val questions = getQuestions()
    private val chosenAs = IntArray(10)

    private val _uiState = MutableStateFlow(QuizState(
        numeralsLanguage = repo.getNumeralsLanguage()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        Arrays.fill(chosenAs, -1)

        ask(0)
    }

    private fun ask(num: Int) {
        _uiState.update { it.copy(
            questionIdx = num
        )}

        updateState()
    }

    fun answered(a: Int) {
        chosenAs[_uiState.value.questionIdx] = a

        _uiState.update { it.copy(
            allAnswered = !chosenAs.contains(-1)
        )}

        if (_uiState.value.questionIdx == 9) {
            _uiState.update { it.copy(
                selection = a,
                nextBtnEnabled = !(it.questionIdx == 9 && !it.allAnswered),
            )}
        }
        else nextQ()
    }

    fun nextQ() {
        if (_uiState.value.questionIdx == 9) {
            if (_uiState.value.allAnswered) endQuiz()
        }
        else {
            ask(_uiState.value.questionIdx + 1)
        }
    }

    fun previousQ() {
        if (_uiState.value.questionIdx > 0)
            ask(_uiState.value.questionIdx - 1)
    }

    private fun endQuiz() {
        val score = calculateScore()

        navigator.navigate(
            Screen.QuizResult(
                score = score.toString(),
                questions = questions.map { q -> q.questionId }.toIntArray().contentToString(),
                chosenAnswers = chosenAs.toTypedArray().toIntArray().contentToString()
            )
        ) {
            popUpTo(Screen.Quiz.route) {
                inclusive = true
            }
        }
    }

    private fun calculateScore(): Int {
        var score = 0
        questions.forEachIndexed { i, q ->
            if (chosenAs[i] == q.correctAnswerId)
                score++
        }
        return score
    }

    private fun getQuestions(): MutableList<QuizQuestionsDB> {
        val rawQuestions = repo.getQuestions().toMutableList()
        rawQuestions.shuffle()
        return rawQuestions.subList(0, 10)
    }

    private fun updateState() {
        val question = questions[_uiState.value.questionIdx]
        val answers = repo.getAnswers(question.questionId)

        _uiState.update { it.copy(
            question = question.questionText!!,
            answers = answers.map { a -> a.answerText },
            selection = chosenAs[it.questionIdx],
            prevBtnEnabled = it.questionIdx != 0,
            nextBtnEnabled = !(it.questionIdx == 9 && !it.allAnswered),
        )}
    }

}