package bassamalim.hidaya.features.quiz.lobby.domain

import bassamalim.hidaya.core.data.repositories.AnalyticsRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import bassamalim.hidaya.core.models.AnalyticsEvent
import javax.inject.Inject

class QuizLobbyDomain @Inject constructor(
    private val quizRepository: QuizRepository,
    private val analyticsRepository: AnalyticsRepository
) {

    suspend fun getQuizCategories() = quizRepository.getQuestionTypes()

    fun trackQuizCategoryViewed(category: String) {
        analyticsRepository.trackEvent(AnalyticsEvent.QuizCategoryStarted(category))
    }

}