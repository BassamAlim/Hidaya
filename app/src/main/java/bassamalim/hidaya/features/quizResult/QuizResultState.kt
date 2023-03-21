package bassamalim.hidaya.features.quizResult

import bassamalim.hidaya.core.models.QuizResultQuestion

data class QuizResultState(
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
    val chosenAs: List<Int> = emptyList()
)
