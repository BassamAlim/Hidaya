package bassamalim.hidaya.features.quiz.result.ui

import bassamalim.hidaya.core.models.QuizAnswer

data class QuizResultQuestion(
    val questionNum: Int,
    val questionText: String,
    val answers: List<QuizAnswer>,
    val chosenAnswerId: Int
)