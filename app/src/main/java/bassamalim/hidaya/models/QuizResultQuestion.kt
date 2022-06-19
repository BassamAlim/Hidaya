package bassamalim.hidaya.models

data class QuizResultQuestion(
    private val questionNumber: Int,
    private val questionText: String,
    private val correctAnswer: Int,
    private val chosenAnswer: Int,
    private val answer1: String,
    private val answer2: String,
    private val answer3: String,
    private val answer4: String
) {
    fun getQuestionNumber(): Int {
        return questionNumber
    }

    fun getQuestionText(): String {
        return questionText
    }

    fun getCorrectAnswer(): Int {
        return correctAnswer
    }

    fun getChosenAnswer(): Int {
        return chosenAnswer
    }

    fun getAnswer1(): String {
        return answer1
    }

    fun getAnswer2(): String {
        return answer2
    }

    fun getAnswer3(): String {
        return answer3
    }

    fun getAnswer4(): String {
        return answer4
    }
}