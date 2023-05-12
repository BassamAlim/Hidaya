package bassamalim.hidaya.features.leaderboard

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepo @Inject constructor(
    res: Resources,
    private val sp: SharedPreferences,
    private val firestore: FirebaseFirestore
) {

    val userStr = res.getString(R.string.user)
    val pagesStr = res.getString(R.string.pages)

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

    fun getLocalRecord(): UserRecord {
        return UserRecord(
            userId = -1,
            readingRecord = PrefUtils.getInt(sp, Prefs.QuranPagesRecord),
            listeningRecord = PrefUtils.getLong(sp, Prefs.TelawatPlaybackRecord)
        )
    }

    fun setLocalQuranRecord(pages: Int) {
        sp.edit()
            .putInt(Prefs.QuranPagesRecord.key, pages)
            .apply()
    }

    fun setLocalTelawatRecord(seconds: Long) {
        sp.edit()
            .putLong(Prefs.TelawatPlaybackRecord.key, seconds)
            .apply()
    }

    suspend fun getUserId(deviceId: String): Int {
        var userId = -1

        firestore.collection("Leaderboard")
            .whereEqualTo("device_id", deviceId)
            .get()
            .addOnSuccessListener { result ->
                val data = result.documents.first().data
                userId = data!!["user_id"].toString().toInt()
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
            .await()

        return userId
    }

    suspend fun getRemoteUserRecord(deviceId: String): UserRecord {
        var record = UserRecord(0, 0, 0)

        firestore.collection("Leaderboard")
            .whereEqualTo("device_id", deviceId)
            .get()
            .addOnSuccessListener { result ->
                val data = result.documents.first().data
                record = UserRecord(
                    userId = data!!["user_id"].toString().toInt(),
                    readingRecord = data["reading_record"].toString().toInt(),
                    listeningRecord = data["listening_record"].toString().toLong()
                )
                println("Data retrieved successfully!")
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
            .await()

        return record
    }

    suspend fun setRemoteUserRecord(deviceId: String, record: UserRecord) {
        firestore.collection("Leaderboard")
            .whereEqualTo("device_id", deviceId)
            .get()
            .addOnSuccessListener { result ->
                val data = result.documents.first().data
                firestore.collection("Leaderboard")
                    .document(data!!["user_id"].toString())
                    .update(
                        mapOf(
                            "reading_record" to record.readingRecord,
                            "listening_record" to record.listeningRecord
                        )
                    )
                    .addOnSuccessListener {
                        println("DocumentSnapshot successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        println("Error updating document: $e")
                    }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
            .await()
    }

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

    suspend fun deviceRegistered(deviceId: String): Boolean {
        var registered = true

        firestore.collection("Leaderboard")
            .whereEqualTo("device_id", deviceId)
            .get()
            .addOnSuccessListener { result ->
                registered = !result.isEmpty
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
            .await()

        return registered
    }

    suspend fun registerDevice(deviceId: String) {
        val localRecord = getLocalRecord()
        val userId = getLargestUserId() + 1

        firestore.collection("Leaderboard")
            .add(
                mapOf(
                    "device_id" to deviceId,
                    "user_id" to userId,
                    "reading_record" to localRecord.readingRecord,
                    "listening_record" to localRecord.listeningRecord
                )
            )
            .addOnSuccessListener {
                println("DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                println("Error writing document: $e")
            }
            .await()
    }

    private suspend fun getLargestUserId(): Int {
        var max = 0

        firestore.collection("Leaderboard")
            .orderBy("user_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                max = result.documents.first().data!!["user_id"].toString().toInt()

                println("Data retrieved successfully!")
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
            .await()

        return max
    }

}