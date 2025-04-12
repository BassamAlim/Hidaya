package bassamalim.hidaya.core.data.repositories

import android.app.Application
import android.util.Log
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OsUtils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val app: Application,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val firestore: FirebaseFirestore,
    @ApplicationScope private val scope: CoroutineScope
) {

    fun getLocalRecord() = userPreferencesDataSource.getUserRecord()

    fun setLocalRecord(userRecord: UserRecord) {
        scope.launch {
            userPreferencesDataSource.updateUserRecord(userRecord)
        }
    }

    fun getRecitationsRecord() = getLocalRecord().map { it.recitationsTime }

    fun setRecitationsRecord(recitationsTime: Long) {
        scope.launch {
            userPreferencesDataSource.updateUserRecord(
                getLocalRecord().first().copy(recitationsTime = recitationsTime)
            )
        }
    }

    fun getRemoteRecord(deviceId: String): Flow<Response<UserRecord>>? {
        if (!OsUtils.isNetworkAvailable(app)) return null

        return firestore.collection("Leaderboard")
            .document(deviceId)
            .snapshots()
            .map {
                if (it.data == null) {
                    Response.Error("Device not registered")
                }
                else {
                    val data = it.data!!
                    Response.Success(
                        UserRecord(
                            userId = data["user_id"].toString().toInt(),
                            quranPages = data["reading_record"].toString().toInt(),
                            recitationsTime = data["listening_record"].toString().toLong()
                        )
                    )
                }
            }
    }

    fun setRemoteRecord(deviceId: String, record: UserRecord) {
        if (!OsUtils.isNetworkAvailable(app)) return

        scope.launch {
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
                    Log.i(Globals.TAG, "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.e(Globals.TAG, "Error getting documents: $e")
                }
                .await()
        }
    }

    suspend fun registerDevice(deviceId: String): UserRecord? {
        if (!OsUtils.isNetworkAvailable(app)) return null

        val localRecord = getLocalRecord().first()

        return try {
            firestore.runTransaction { transaction ->
                val counterDocRef = firestore.collection("Counters").document("users")
                val counterSnapshot = transaction.get(counterDocRef)

                if (!counterSnapshot.exists() || counterSnapshot.data == null) {
                    throw FirebaseFirestoreException(
                        "Counter document not found",
                        FirebaseFirestoreException.Code.NOT_FOUND
                    )
                }

                val lastId = counterSnapshot.data!!["last_id"].toString().toInt()
                val newUserId = lastId + 1

                // Update the counter document with the new last ID
                transaction.update(counterDocRef, "last_id", newUserId)

                // Create the new user record in Leaderboard collection
                val leaderboardDocRef = firestore.collection("Leaderboard").document(deviceId)
                transaction.set(leaderboardDocRef, mapOf(
                    "user_id" to newUserId,
                    "reading_record" to localRecord.quranPages,
                    "listening_record" to localRecord.recitationsTime,
                    "created_at" to System.currentTimeMillis()
                ))

                // Return the created user record
                UserRecord(
                    userId = newUserId,
                    quranPages = localRecord.quranPages,
                    recitationsTime = localRecord.recitationsTime
                )
            }.await()
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Transaction failed: $e")
            null
        }
    }

    suspend fun getReadingRanks(
        previousLast: DocumentSnapshot? = null
    ): Pair<Response<Map<Int, Long>>, DocumentSnapshot?>? {
        if (!OsUtils.isNetworkAvailable(app)) return null

        var last: DocumentSnapshot? = null

        var query = firestore.collection("Leaderboard")
            .orderBy("reading_record", Query.Direction.DESCENDING)
            .orderBy("user_id")
        if (previousLast != null) query = query.startAfter(previousLast)
        val results = query
            .limit(100)
            .get()
            .await()
            .let { result ->
                try {
                    last = result.documents.last()
                    Response.Success(
                        result.documents.associate { document ->
                            document.data!!["user_id"].toString().toInt() to
                                    document.data!!["reading_record"].toString().toLong()
                        }
                    )
                } catch (e: Exception) {
                    Log.i(Globals.TAG, "Error getting documents: ${e.message}")
                    Response.Error("Error fetching data")
                }
            }
        return Pair(results, last)
    }

    suspend fun getListeningRanks(
        previousLast: DocumentSnapshot? = null
    ): Pair<Response<Map<Int, Long>>, DocumentSnapshot?>? {
        if (!OsUtils.isNetworkAvailable(app)) return null

        var last: DocumentSnapshot? = null

        var query = firestore.collection("Leaderboard")
            .orderBy("listening_record", Query.Direction.DESCENDING)
            .orderBy("user_id")
        if (previousLast != null) query = query.startAfter(previousLast)
        val results = query
            .limit(100)
            .get()
            .await()
            .let { result ->
                try {
                    last = result.documents.last()
                    Response.Success(
                        result.documents.associate { document ->
                            document.data!!["user_id"].toString().toInt() to
                                    document.data!!["listening_record"].toString().toLong()
                        }
                    )
                } catch (e: Exception) {
                    Log.i(Globals.TAG, "Error getting documents: ${e.message}")
                    Response.Error("Error fetching data")
                }
            }
        return Pair(results, last)
    }

}