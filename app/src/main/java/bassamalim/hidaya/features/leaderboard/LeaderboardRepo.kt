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

    /*
    * Returns user record if the device is registered in the database, otherwise null
    */
    suspend fun getRemoteUserRecord(deviceId: String): UserRecord? {
        var record: UserRecord? = null

        firestore.collection("Leaderboard")
            .document(deviceId)
            .get()
            .addOnSuccessListener { result ->
                val data = result.data
                if (data != null) {
                    record = UserRecord(
                        userId = data["user_id"].toString().toInt(),
                        readingRecord = data["reading_record"].toString().toInt(),
                        listeningRecord = data["listening_record"].toString().toLong()
                    )
                }

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
            .document(deviceId)
            .set(
                mapOf(
                    "reading_record" to record.readingRecord,
                    "listening_record" to record.listeningRecord
                )
            )
            .addOnSuccessListener {
                println("DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
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

    suspend fun registerDevice(deviceId: String): UserRecord {
        println("registerDevice()")

        val localRecord = getLocalRecord()
        val userId = getLargestUserId() + 1
        val remoteUserRecord = UserRecord(
            userId = userId,
            readingRecord = localRecord.readingRecord,
            listeningRecord = localRecord.listeningRecord
        )

        firestore.collection("Leaderboard")
            .document(deviceId)
            .set(
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

        return remoteUserRecord
    }

    private suspend fun getLargestUserId(): Int {
        var max = 0

        firestore.collection("Leaderboard")
            .orderBy("user_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty())
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