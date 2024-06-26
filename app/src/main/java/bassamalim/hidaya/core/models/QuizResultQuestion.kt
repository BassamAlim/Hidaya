package bassamalim.hidaya.core.models

data class QuizResultQuestion(
    val questionNum: Int,
    val questionText: String,
    val correctAns: Int,
    val chosenAns: Int,
    val answers: List<String>
)