package bassamalim.hidaya.state

import bassamalim.hidaya.models.QuizResultQuestion

data class QuizResultState(
    val score: String = 0,
    val questions: List<QuizResultQuestion> = emptyList(),
    val chosenAs: List<Int> = emptyList()
)
