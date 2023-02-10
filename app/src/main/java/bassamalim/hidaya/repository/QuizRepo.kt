package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class QuizRepo @Inject constructor(
    private val resources: Resources,
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    fun getNumeralsLanguage() = PrefUtils.getNumeralsLanguage(pref)

    fun getQuestions() = db.quizQuestionDao().all

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

    fun getQuestionStr() = resources.getString(R.string.question)

}