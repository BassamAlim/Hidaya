package bassamalim.hidaya.features.quiz.quizTester.domain

import bassamalim.hidaya.core.data.database.models.QuizQuestion
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuizDomain @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val quizRepository: QuizRepository
) {

    val chosenAs = IntArray(10) { -1 }
    val questions = getQuestions()

    fun calculateScore(): Int {
        var score = 0
        questions.forEachIndexed { i, q ->
            if (chosenAs[i] == q.correctAnswerId)
                score++
        }
        return score
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    private fun getQuestions(): MutableList<QuizQuestion> {
        val allQuestions = quizRepository.getQuestions().toMutableList()
        allQuestions.shuffle()
        return allQuestions.subList(0, 10)
    }

    fun getAnswers(questionId: Int) = quizRepository.getAnswers(questionId)

}