package bassamalim.hidaya.features.home

import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.QuranPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.UserPreferencesRepository
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.leaderboard.UserRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val resources: Resources,
    private val appSettingsPrefsRpo: AppSettingsPreferencesRepository,
    private val quranPrefsRepo: QuranPreferencesRepository,
    private val userPrefsRepo: UserPreferencesRepository,
    private val firestore: FirebaseFirestore
) {

    suspend fun getIsWerdDone() =
        quranPrefsRepo.flow.first()
            .isWerdDone

    fun getPrayerNames(): Array<String> =
        resources.getStringArray(R.array.prayer_names)

    suspend fun getNumeralsLanguage() =
        appSettingsPrefsRpo.flow.first()
            .numeralsLanguage

    suspend fun getWerdPage() =
        quranPrefsRepo.flow.first()
            .werdPage

    suspend fun getQuranPagesRecord() =
        userPrefsRepo.flow.first()
            .quranPagesRecord

    suspend fun getTelawatTimeRecord() =
        userPrefsRepo.flow.first()
            .recitationsTimeRecord

    suspend fun getLocation() =
        userPrefsRepo.flow.first()
            .location

    suspend fun getLocalRecord(): UserRecord {
        return UserRecord(
            userId = -1,
            readingRecord = getQuranPagesRecord(),
            listeningRecord = getTelawatTimeRecord()
        )
    }

    suspend fun setLocalQuranRecord(pages: Int) {
        userPrefsRepo.update { it.copy(
            quranPagesRecord = pages
        )}
    }

    suspend fun setLocalTelawatRecord(seconds: Long) {
        userPrefsRepo.update { it.copy(
            recitationsTimeRecord = seconds
        )}
    }

    suspend fun getRemoteUserRecord(deviceId: String): Response<UserRecord> {
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
                                readingRecord = data["reading_record"].toString().toInt(),
                                listeningRecord = data["listening_record"].toString().toLong()
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            Log.e(Global.TAG, "Error getting documents: $e")
            Response.Error("Error fetching data")
        }
    }

    suspend fun setRemoteUserRecord(deviceId: String, record: UserRecord) {
        firestore.collection("Leaderboard")
            .document(deviceId)
            .set(
                mapOf(
                    "user_id" to record.userId,
                    "reading_record" to record.readingRecord,
                    "listening_record" to record.listeningRecord
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
        val localRecord = getLocalRecord()

        val largestUserId = getLargestUserId() ?: return null

        val userId = largestUserId + 1
        val remoteUserRecord = UserRecord(
            userId = userId,
            readingRecord = localRecord.readingRecord,
            listeningRecord = localRecord.listeningRecord
        )

        firestore.collection("Leaderboard")
            .document(deviceId)
            .set(
                mapOf(
                    "user_id" to userId,
                    "reading_record" to localRecord.readingRecord,
                    "listening_record" to localRecord.listeningRecord
                )
            )
            .addOnSuccessListener {
                Log.i(Global.TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.e(Global.TAG, "Error writing document: $e")
            }
            .await()

        return remoteUserRecord
    }

    private suspend fun getLargestUserId(): Int? {
        var max: Int? = null

        firestore.collection("Leaderboard")
            .orderBy("user_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty())
                    max = result.documents.first().data!!["user_id"].toString().toInt()

                Log.i(Global.TAG, "Data retrieved successfully!")
            }
            .addOnFailureListener { exception ->
                Log.e(Global.TAG, "Error getting documents: $exception")
            }
            .await()

        return max
    }

}