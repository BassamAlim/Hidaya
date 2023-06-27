package bassamalim.hidaya.features.leaderboard

import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepo @Inject constructor(
    res: Resources,
    sp: SharedPreferences,
    private val firestore: FirebaseFirestore
) {

    val userStr = res.getString(R.string.user)
    val pagesStr = res.getString(R.string.pages)
    val errorFetchingDataStr = res.getString(R.string.error_fetching_data)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

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