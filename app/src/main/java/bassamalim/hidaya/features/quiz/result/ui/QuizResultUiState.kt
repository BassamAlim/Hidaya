package bassamalim.hidaya.features.quiz.result.ui

data class QuizResultUiState(
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
)
