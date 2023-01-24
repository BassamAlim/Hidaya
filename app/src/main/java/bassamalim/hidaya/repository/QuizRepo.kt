package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class QuizRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    fun getQuestionStr() = context.getString(R.string.question)

    fun getNumeralsLanguage() = PrefUtils.getNumeralsLanguage(pref)

    fun getQuestions() = db.quizQuestionDao().all

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

}