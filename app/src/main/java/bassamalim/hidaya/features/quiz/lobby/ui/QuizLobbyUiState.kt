package bassamalim.hidaya.features.quiz.lobby.ui

data class QuizLobbyUiState(
    val isLoading: Boolean = true,
    val quizCategories: List<String> = emptyList()
)
