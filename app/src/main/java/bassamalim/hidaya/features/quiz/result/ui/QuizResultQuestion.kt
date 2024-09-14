package bassamalim.hidaya.features.quiz.result.ui

data class QuizResultQuestion(
    val questionNum: Int,
    val questionText: String,
    val answers: List<String>,
    val correctAnswerId: Int,
    val chosenAnswerId: Int
)