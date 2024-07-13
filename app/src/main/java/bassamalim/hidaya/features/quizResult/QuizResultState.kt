package bassamalim.hidaya.features.quizResult

data class QuizResultState(
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
    val chosenAs: List<Int> = emptyList()
)
