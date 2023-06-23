package bassamalim.hidaya.features.quizResult

data class QuizResultArgs(
    val score: Int,
    val questions: IntArray,
    val chosenAnswers: IntArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuizResultArgs

        if (score != other.score) return false
        if (!questions.contentEquals(other.questions)) return false
        if (!chosenAnswers.contentEquals(other.chosenAnswers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + questions.contentHashCode()
        result = 31 * result + chosenAnswers.contentHashCode()
        return result
    }

}
