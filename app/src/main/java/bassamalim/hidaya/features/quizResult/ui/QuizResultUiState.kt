package bassamalim.hidaya.features.quizResult.ui

data class QuizResultUiState(
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
)
