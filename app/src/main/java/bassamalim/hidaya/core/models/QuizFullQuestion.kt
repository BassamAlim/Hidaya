package bassamalim.hidaya.core.models

data class QuizFullQuestion(
    val id: Int,
    val question: String,
    val answers: List<QuizAnswer>
)