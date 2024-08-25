package bassamalim.hidaya.features.quiz.quizResult.ui

data class QuizResultQuestion(
    val questionNum: Int,
    val questionText: String,
    val correctAns: Int,
    val chosenAns: Int,
    val answers: List<String>
)