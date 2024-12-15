package bassamalim.hidaya.features.quiz.test.ui

import bassamalim.hidaya.core.enums.Language

data class QuizTestUiState(
    val isLoading: Boolean = true,
    val titleQuestionNumber: String = "",
    val questionIdx: Int = 0,
    val question: String = "",
    val answers: List<String> = emptyList(),
    val selection: Int = -1,
    val allAnswered: Boolean = false,
    val previousButtonEnabled: Boolean = false,
    val nextButtonEnabled: Boolean = true,
    val numeralsLanguage: Language = Language.ARABIC
)