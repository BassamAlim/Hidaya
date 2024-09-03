package bassamalim.hidaya.features.quiz.quizResult.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuizResultDomain @Inject constructor(
    private val quizRepository: QuizRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getQuestions(ids: IntArray) = ids.map { id -> quizRepository.getQuestion(id) }

    fun getAnswers(questionId: Int) = quizRepository.getAnswers(questionId)

}