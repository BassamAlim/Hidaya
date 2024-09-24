package bassamalim.hidaya.features.quiz.result.ui

data class QuizResultUiState(
    val isLoading: Boolean = true,
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
)
