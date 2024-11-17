package bassamalim.hidaya.features.quiz.test.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuizFullQuestion
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils
import bassamalim.hidaya.features.quiz.test.domain.QuizTestDomain
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
class QuizTestViewModel @Inject constructor(
    private val domain: QuizTestDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var questions: List<QuizFullQuestion>
    private val chosenAs = IntArray(10) { -1 }
    private lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(QuizTestUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuizTestUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()
            questions = domain.getQuizQuestions()

            _uiState.update { it.copy(
                isLoading = false
            )}

            updateState()
        }
    }

    fun onPreviousQuestionClick() {
        if (_uiState.value.questionIdx > 0)
            ask(_uiState.value.questionIdx - 1)
    }

    fun onNextQuestionClick() {
        if (_uiState.value.questionIdx == 9) {
            if (_uiState.value.allAnswered) endQuiz()
        }
        else ask(_uiState.value.questionIdx + 1)
    }

    fun onAnswerSelected(answerIndex: Int) {
        chosenAs[_uiState.value.questionIdx] = answerIndex

        _uiState.update { it.copy(
            allAnswered = !chosenAs.contains(-1)
        )}

        if (_uiState.value.questionIdx == 9) {
            _uiState.update { it.copy(
                selection = answerIndex,
                nextBtnEnabled = !(it.questionIdx == 9 && !it.allAnswered),
            )}
        }
        else onNextQuestionClick()
    }

    private fun ask(num: Int) {
        _uiState.update { it.copy(
            questionIdx = num
        )}

        updateState()
    }

    private fun endQuiz() {
        val score = domain.calculateScore(questions, chosenAs)
        val questionsJson = Gson().toJson(
            questions.toTypedArray(),
            Array<QuizFullQuestion>::class.java
        )

        navigator.navigate(
            Screen.QuizResult(
                score = score.toString(),
                questions = questionsJson,
                chosenAnswers = chosenAs.toTypedArray().toIntArray().contentToString()
            )
        ) {
            popUpTo(Screen.QuizTest.route) {
                inclusive = true
            }
        }
    }

    private fun updateState() {
        val question = questions[_uiState.value.questionIdx]

        _uiState.update { it.copy(
            titleQuestionNumber = LangUtils.translateNums(
                numeralsLanguage = numeralsLanguage,
                string = (it.questionIdx + 1).toString()
            ),
            question = question.question,
            answers = question.answers.map { answer -> answer.text },
            selection = chosenAs[it.questionIdx],
            prevBtnEnabled = it.questionIdx != 0,
            nextBtnEnabled = !(it.questionIdx == 9 && !it.allAnswered),
        )}
    }

}