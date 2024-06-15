package bassamalim.hidaya.features.home

import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.features.leaderboard.UserRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepo @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore
) {

    fun getIsWerdDone() =
        preferencesDS.getBoolean(Preference.WerdDone)

    fun getPrayerNames(): Array<String> =
        res.getStringArray(R.array.prayer_names)

    fun getNumeralsLanguage() =
        Language.valueOf(preferencesDS.getString(Preference.NumeralsLanguage))

    fun getTodayWerdPage() =
        preferencesDS.getInt(Preference.WerdPage)

    fun getQuranPagesRecord() =
        preferencesDS.getInt(Preference.QuranPagesRecord)

    fun getTelawatPlaybackRecord() =
        preferencesDS.getLong(Preference.TelawatPlaybackRecord)

    fun getLocation() =
        LocUtils.retrieveLocation(preferencesDS.getString(Preference.StoredLocation))

    fun getLocalRecord(): UserRecord {
        return UserRecord(
            userId = -1,
            readingRecord = preferencesDS.getInt(Preference.QuranPagesRecord),
            listeningRecord = preferencesDS.getLong(Preference.TelawatPlaybackRecord)
        )
    }

    fun setLocalQuranRecord(pages: Int) {
        preferencesDS.setInt(Preference.QuranPagesRecord, pages)
    }

    fun setLocalTelawatRecord(seconds: Long) {
        preferencesDS.setLong(Preference.TelawatPlaybackRecord, seconds)
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

        val largestUserId = getLargestUserId()
        if (largestUserId == null) return null

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