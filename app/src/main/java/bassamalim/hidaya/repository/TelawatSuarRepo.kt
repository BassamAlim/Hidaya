package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.database.AppDatabase
import com.google.gson.Gson
import javax.inject.Inject

class TelawatSuarRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

}