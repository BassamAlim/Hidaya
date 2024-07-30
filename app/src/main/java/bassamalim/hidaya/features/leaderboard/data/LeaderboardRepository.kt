package bassamalim.hidaya.features.leaderboard.data

import android.util.Log
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.other.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepository @Inject constructor(
    private val appSettingsPrefsRepo: AppSettingsPreferencesDataSource,
    private val firestore: FirebaseFirestore
) {

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.getNumeralsLanguage().first()

    suspend fun getUserRecord(deviceId: String): Response<UserRecord> {
        return try {
            firestore.collection("Leaderboard")
                .document(deviceId)
                .get()
                .await()
                .let { result ->
                    if (result.data == null) {
                        Response.Error("Device not registered")
                    }
                    else {
                        val data = result.data!!
                        Response.Success(
                            UserRecord(
                                userId = data["user_id"].toString().toInt(),
                                quranPages = data["reading_record"].toString().toInt(),
                                recitationsTime = data["listening_record"].toString().toLong()
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            Log.e(Global.TAG, "Error getting documents: $e")
            Response.Error("Error fetching data")
        }
    }

    suspend fun getRanks(): Response<List<UserRecord>> {
        return firestore.collection("Leaderboard")
            .get()
            .await()
            .let { result ->
                try {
                    Response.Success(
                        result.documents.map { document ->
                            UserRecord(
                                userId = document.data!!["user_id"].toString().toInt(),
                                quranPages = document.data!!["reading_record"]
                                    .toString().toInt(),
                                recitationsTime = document.data!!["listening_record"]
                                    .toString().toLong()
                            )
                        }
                    )
                } catch (e: Exception) {
                    Log.i(Global.TAG, "Error getting documents: ${e.message}")
                    Response.Error("Error fetching data")
                }
            }
    }

}