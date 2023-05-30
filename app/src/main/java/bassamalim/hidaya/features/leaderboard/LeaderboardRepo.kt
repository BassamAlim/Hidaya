package bassamalim.hidaya.features.leaderboard

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
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
        val items = mutableListOf<UserRecord>()

        firestore.collection("Leaderboard")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    items.add(
                        UserRecord(
                            userId = document.data["user_id"].toString().toInt(),
                            readingRecord = document.data["reading_record"].toString().toInt(),
                            listeningRecord = document.data["listening_record"].toString().toLong()
                        )
                    )
                }
                println("Data retrieved successfully!")
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
            .await()

        return items
    }

}