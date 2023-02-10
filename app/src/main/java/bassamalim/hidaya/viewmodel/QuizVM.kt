package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.Screen
import bassamalim.hidaya.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.repository.QuizRepo
import bassamalim.hidaya.state.QuizState
import bassamalim.hidaya.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

@HiltViewModel
class QuizVM @Inject constructor(
    private val repository: QuizRepo
): ViewModel() {

    private val questionStr = repository.getQuestionStr()
    private val numeralsLanguage = repository.getNumeralsLanguage()
    private val questions = getQuestions()
    private val chosenAs = IntArray(10)
    private var current = 0
    private var allAnswered = false

    private val _uiState = MutableStateFlow(QuizState())
    val uiState = _uiState.asStateFlow()

    init {
        Arrays.fill(chosenAs, -1)

        ask(0)
    }

    private fun ask(num: Int) {
        current = num

        updateState()
    }

    fun answered(a: Int, nc: NavController) {
        chosenAs[current] = a

        allAnswered = !chosenAs.contains(-1)

        if (current == 9) {
            _uiState.update { it.copy(
                selection = a,
                nextBtnEnabled = !(current == 9 && !allAnswered),
                nextBtnTextResId = getNextBtnTextResId()
            )}
        }
        else nextQ(nc)
    }

    fun nextQ(navController: NavController) {
        if (current == 9) {
            if (allAnswered) endQuiz(navController)
        }
        else ask(++current)
    }

    fun previousQ() {
        if (current > 0) ask(--current)
    }

    private fun endQuiz(navController: NavController) {
        val score = calculateScore()

        navController.navigate(
            Screen.QuizResult(
                score.toString(),
                questions.map { q -> q.getQuestionId() }.toIntArray().contentToString(),
                chosenAs.toString()
            ).route
        ) {
            popUpTo(
                Screen.QuizResult(
                    score.toString(),
                    questions.map { q -> q.getQuestionId() }.toIntArray().contentToString(),
                    chosenAs.toString()
                ).route
            ) {
                inclusive = true
            }
        }
    }

    private fun calculateScore(): Int {
        var score = 0
        questions.forEachIndexed { i, q ->
            if (chosenAs[i] == q.getCorrectAnswerId())
                score++
        }
        return score
    }

    private fun getQuestions(): MutableList<QuizQuestionsDB> {
        val rawQuestions = repository.getQuestions().toMutableList()
        rawQuestions.shuffle()
        return rawQuestions.subList(0, 10)
    }

    private fun updateState() {
        val question = questions[current]
        val answers = repository.getAnswers(question.getQuestionId())

        _uiState.update { it.copy(
            questionNumText =
            "$questionStr ${translateNums(numeralsLanguage, (current + 1).toString())}",
            question = question.getQuestionText(),
            answers = answers.map { a -> a.answer_text!! },
            selection = chosenAs[current],
            prevBtnEnabled = current != 0,
            nextBtnEnabled = !(current == 9 && !allAnswered),
            nextBtnTextResId = getNextBtnTextResId()
        )}
    }

    private fun getNextBtnTextResId(): Int {
        return if (current == 9) {
            if (allAnswered) R.string.finish_quiz
            else R.string.answer_all_questions
        }
        else R.string.next_question
    }

}