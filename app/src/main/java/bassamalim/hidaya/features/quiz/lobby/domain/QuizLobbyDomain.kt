package bassamalim.hidaya.features.quiz.lobby.domain

import bassamalim.hidaya.core.data.repositories.QuizRepository
import javax.inject.Inject

class QuizLobbyDomain @Inject constructor(
    private val quizRepository: QuizRepository
) {

    suspend fun getQuizCategories() = quizRepository.getQuestionTypes()

}