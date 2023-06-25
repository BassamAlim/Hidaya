package bassamalim.hidaya.features.quizResult

data class QuizResultNavArgs(
    val score: Int,
    val questionIds: IntArray,
    val chosenAnswers: IntArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuizResultNavArgs

        if (score != other.score) return false
        if (!questionIds.contentEquals(other.questionIds)) return false
        if (!chosenAnswers.contentEquals(other.chosenAnswers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score
        result = 31 * result + questionIds.contentHashCode()
        result = 31 * result + chosenAnswers.contentHashCode()
        return result
    }

}