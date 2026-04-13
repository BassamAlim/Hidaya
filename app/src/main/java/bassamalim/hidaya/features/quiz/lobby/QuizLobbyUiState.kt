package bassamalim.hidaya.features.quiz.lobby

data class QuizLobbyUiState(
    val isLoading: Boolean = true,
    val quizCategories: List<String> = emptyList()
)
