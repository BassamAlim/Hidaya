package bassamalim.hidaya.features.quiz.quizTest.domain

import bassamalim.hidaya.core.data.database.models.QuizQuestion
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuizTestDomain @Inject constructor(
    private val quizRepository: QuizRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    fun calculateScore(questions: List<QuizQuestion>, chosenAs: IntArray): Int {
        var score = 0
        questions.forEachIndexed { i, q ->
            if (chosenAs[i] == q.correctAnswerId)
                score++
        }
        return score
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getQuestions(): MutableList<QuizQuestion> {
        val allQuestions = quizRepository.getQuestions().toMutableList()
        allQuestions.shuffle()
        return allQuestions.subList(0, 10)
    }

    fun getAnswers(questionId: Int) = quizRepository.getAnswers(questionId)

}