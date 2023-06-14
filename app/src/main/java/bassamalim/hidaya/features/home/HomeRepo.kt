package bassamalim.hidaya.features.home

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import bassamalim.hidaya.features.leaderboard.UserRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepo @Inject constructor(
    private val res: Resources,
    val sp: SharedPreferences,
    val db: AppDatabase,
    private val firestore: FirebaseFirestore
) {

    fun getIsWerdDone() =
        PrefUtils.getBoolean(sp, Prefs.WerdDone)

    fun getPrayerNames(): Array<String> =
        res.getStringArray(R.array.prayer_names)

    fun getNumeralsLanguage() = Language.valueOf(
        PrefUtils.getString(sp, Prefs.NumeralsLanguage)
    )

    fun getTodayWerdPage() =
        PrefUtils.getInt(sp, Prefs.WerdPage)

    fun getQuranPagesRecord() =
        PrefUtils.getInt(sp, Prefs.QuranPagesRecord)

    fun getTelawatPlaybackRecord() =
        PrefUtils.getLong(sp, Prefs.TelawatPlaybackRecord)

    fun getLocation() =
        LocUtils.retrieveLocation(sp)

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

    suspend fun getRemoteUserRecord(deviceId: String): Response<UserRecord> {
        return try {
            firestore.collection("Leaderboard")
                .document(deviceId)
                .get()
                .await()
                .let { result ->
                    if (result.data == null) Response.Error("Error fetching data")
                    else if (result.data!!.isEmpty()) Response.Error("Device not registered")
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
            println("Error getting documents: $e")
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
                println("DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
            .await()
    }

    suspend fun registerDevice(deviceId: String): UserRecord {
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