package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.database.daos.QuizQuestionsDao
import bassamalim.hidaya.core.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuizRepository @Inject constructor(
    private val quizQuestionsDao: QuizQuestionsDao,
    private val quizAnswerDao: QuizAnswersDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun getQuestions() = withContext(dispatcher) {
        quizQuestionsDao.getAll()
    }

    suspend fun getQuestion(questionId: Int) = withContext(dispatcher) {
        quizQuestionsDao.getQuestion(questionId)
    }

    suspend fun getAnswers(questionId: Int) = withContext(dispatcher) {
        quizAnswerDao.getAnswers(questionId)
    }

}