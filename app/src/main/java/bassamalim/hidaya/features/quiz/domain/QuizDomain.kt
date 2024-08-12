package bassamalim.hidaya.features.quiz.domain

import bassamalim.hidaya.core.data.database.models.QuizQuestion
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuizDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val quizRepo: QuizRepository
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

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getQuestions(): MutableList<QuizQuestion> {
        val allQuestions = quizRepo.getQuestions().toMutableList()
        allQuestions.shuffle()
        return allQuestions.subList(0, 10)
    }

    fun getAnswers(questionId: Int) = quizRepo.getAnswers(questionId)

}