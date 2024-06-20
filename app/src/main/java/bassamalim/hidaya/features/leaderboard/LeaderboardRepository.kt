package bassamalim.hidaya.features.leaderboard

import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.other.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepository @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource,
    private val firestore: FirebaseFirestore
) {

    val userStr = res.getString(R.string.user)
    val errorFetchingDataStr = res.getString(R.string.error_fetching_data)

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    suspend fun getRanks(): List<UserRecord> {
        return firestore.collection("Leaderboard")
            .get()
            .await()
            .let { result ->
                try {
                    result.documents.map { document ->
                        UserRecord(
                            userId = document.data!!["user_id"].toString().toInt(),
                            readingRecord = document.data!!["reading_record"].toString().toInt(),
                            listeningRecord = document.data!!["listening_record"].toString().toLong()
                        )
                    }
                } catch (e: Exception) {
                    Log.i(Global.TAG, "Error getting documents: ${e.message}")

                    listOf()
                }
            }
    }

}