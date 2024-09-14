package bassamalim.hidaya.core.data.repositories

import android.util.Log
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.other.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val firestore: FirebaseFirestore,
) {

    fun getLocalRecord() = userPreferencesDataSource.getUserRecord()

    fun getQuranRecord() = getLocalRecord().map { it.quranPages }

    fun getRecitationsRecord() = getLocalRecord().map { it.recitationsTime }

    suspend fun setLocalRecord(userRecord: UserRecord) {
        userPreferencesDataSource.updateUserRecord(userRecord)
    }

    suspend fun setQuranRecord(quranPages: Int) {
        userPreferencesDataSource.updateUserRecord(
            getLocalRecord().first().copy(quranPages = quranPages)
        )
    }

    suspend fun setRecitationsRecord(recitationsTime: Long) {
        userPreferencesDataSource.updateUserRecord(
            getLocalRecord().first().copy(recitationsTime = recitationsTime)
        )
    }

    suspend fun getRemoteRecord(deviceId: String): Response<UserRecord> {
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

    suspend fun setRemoteRecord(deviceId: String, record: UserRecord) {
        firestore.collection("Leaderboard")
            .document(deviceId)
            .set(
                mapOf(
                    "user_id" to record.userId,
                    "reading_record" to record.quranPages,
                    "listening_record" to record.recitationsTime
                )
            )
            .addOnSuccessListener {
                Log.i(Global.TAG, "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.e(Global.TAG, "Error getting documents: $e")
            }
            .await()
    }

    suspend fun registerDevice(deviceId: String): UserRecord? {
        val localRecord = getLocalRecord().first()

        val largestUserId = getLastUserId() ?: return null
        val userId = largestUserId + 1

        val remoteLeaderboardUserRecord = UserRecord(
            userId = userId,
            quranPages = localRecord.quranPages,
            recitationsTime = localRecord.recitationsTime
        )

        firestore.collection("Leaderboard")
            .document(deviceId)
            .set(
                mapOf(
                    "user_id" to userId,
                    "reading_record" to localRecord.quranPages,
                    "listening_record" to localRecord.recitationsTime
                )
            )
            .addOnSuccessListener {
                Log.i(Global.TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.e(Global.TAG, "Error writing document: $e")
            }
            .await()

        return remoteLeaderboardUserRecord
    }

    private suspend fun getLastUserId(): Int? {
        var id: Int? = null

        firestore.collection("Counters")
            .document("users")
            .get()
            .addOnSuccessListener { result ->
                id = result.data!!["last_id"].toString().toInt()
                Log.i(Global.TAG, "Data retrieved successfully!")
            }
            .addOnFailureListener { exception ->
                Log.e(Global.TAG, "Error getting documents: $exception")
            }
            .await()

        return id
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