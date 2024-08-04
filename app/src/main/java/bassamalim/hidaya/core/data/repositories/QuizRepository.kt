package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.database.daos.QuizQuestionsDao
import javax.inject.Inject

class QuizRepository @Inject constructor(
    private val quizQuestionsDao: QuizQuestionsDao,
    private val quizAnswerDao: QuizAnswersDao
) {

    fun getQuestions() = quizQuestionsDao.getAll()

    fun getQuestion(questionId: Int) = quizQuestionsDao.getQuestion(questionId)

    fun getAnswers(questionId: Int) = quizAnswerDao.getAnswers(questionId)

}