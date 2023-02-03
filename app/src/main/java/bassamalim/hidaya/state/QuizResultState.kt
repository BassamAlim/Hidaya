package bassamalim.hidaya.state

import bassamalim.hidaya.models.QuizResultQuestion

data class QuizResultState(
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
    val chosenAs: List<Int> = emptyList()
)
