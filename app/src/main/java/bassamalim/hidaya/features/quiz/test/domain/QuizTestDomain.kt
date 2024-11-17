package bassamalim.hidaya.features.quiz.test.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import bassamalim.hidaya.core.models.QuizFullQuestion
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuizTestDomain @Inject constructor(
    private val quizRepository: QuizRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    fun calculateScore(questions: List<QuizFullQuestion>, chosenAs: IntArray): Int {
        var score = 0
        questions.forEachIndexed { i, q ->
            if (q.answers[chosenAs[i]].isCorrect)
                score++
        }
        return score
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getQuizQuestions(): List<QuizFullQuestion> {
        val ids = getRandomIds()
        return getFullQuestions(ids)
    }

    private suspend fun getRandomIds(): List<Int> {
        val allIds = quizRepository.getQuestionIds().toMutableList()
        allIds.shuffle()
        return allIds.subList(0, 10)
    }

    private suspend fun getFullQuestions(ids: List<Int>): List<QuizFullQuestion> {
        val questions = quizRepository.getFullQuestions(ids.toIntArray())
        return questions.map { question ->
            question.copy(answers = question.answers.shuffled())
        }
    }

}