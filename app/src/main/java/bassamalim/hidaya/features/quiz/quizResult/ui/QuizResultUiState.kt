package bassamalim.hidaya.features.quiz.quizResult.ui

data class QuizResultUiState(
    val score: String = "",
    val questions: List<QuizResultQuestion> = emptyList(),
)
