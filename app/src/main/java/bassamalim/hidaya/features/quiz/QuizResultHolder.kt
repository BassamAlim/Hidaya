package bassamalim.hidaya.features.quiz

import bassamalim.hidaya.core.models.QuizFullQuestion
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory hand-off for quiz results between [QuizTestViewModel] and [QuizResultViewModel].
 *
 * The result payload (the full questions and the user's chosen answers) is too large and
 * contains characters unsafe for a navigation route path, so it is passed through this
 * process-scoped holder instead of being serialized into the route.
 */
@Singleton
class QuizResultHolder @Inject constructor() {
    var result: QuizResult? = null
}

data class QuizResult(
    val score: Int,
    val questions: List<QuizFullQuestion>,
    val chosenAnswers: List<Int>
)
