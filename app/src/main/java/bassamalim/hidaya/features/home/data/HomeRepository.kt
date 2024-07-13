package bassamalim.hidaya.features.home.data

import android.content.res.Resources
import android.util.Log
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.PrayersPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.QuranPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.UserPreferencesRepository
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.leaderboard.LeaderboardUserRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val resources: Resources,
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val appSettingsPrefsRpo: AppSettingsPreferencesRepository,
    private val prayersPreferencesRepository: PrayersPreferencesRepository,
    private val quranPrefsRepo: QuranPreferencesRepository,
    private val userPrefsRepo: UserPreferencesRepository,
) {

    suspend fun getNumeralsLanguage() = appSettingsPrefsRpo.getNumeralsLanguage().first()

    fun getTimeFormat() = appSettingsPrefsRpo.getTimeFormat()

    fun getIsWerdDone() = quranPrefsRepo.getIsWerdDone()

    fun getWerdPage() = quranPrefsRepo.getWerdPage()

    fun getLocalRecord() = userPrefsRepo.getUserRecord()

    fun getLocation() = userPrefsRepo.getLocation()

    suspend fun setLocalRecord(userRecord: UserRecord) {
        userPrefsRepo.update { it.copy(
            userRecord = userRecord
        )}
    }

    fun getPrayerTimesCalculatorSettings() =
        prayersPreferencesRepository.getPrayerTimesCalculatorSettings()

    fun getTimeOffsets() = prayersPreferencesRepository.getTimeOffsets()

    fun getTimeZone(cityId: Int) = database.cityDao().getCity(cityId).timeZone

    suspend fun getRemoteRecord(deviceId: String): Response<LeaderboardUserRecord> {
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
                            LeaderboardUserRecord(
                                userId = data["user_id"].toString().toInt(),
                                quranRecord = data["reading_record"].toString().toInt(),
                                recitationsRecord = data["listening_record"].toString().toLong()
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            Log.e(Global.TAG, "Error getting documents: $e")
            Response.Error("Error fetching data")
        }
    }

    suspend fun setRemoteRecord(deviceId: String, record: LeaderboardUserRecord) {
        firestore.collection("Leaderboard")
            .document(deviceId)
            .set(
                mapOf(
                    "user_id" to record.userId,
                    "reading_record" to record.quranRecord,
                    "listening_record" to record.recitationsRecord
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

    suspend fun registerDevice(deviceId: String): LeaderboardUserRecord? {
        val localRecord = getLocalRecord().first()

        val largestUserId = getLastUserId() ?: return null
        val userId = largestUserId + 1

        val remoteLeaderboardUserRecord = LeaderboardUserRecord(
            userId = userId,
            quranRecord = localRecord.quranPages,
            recitationsRecord = localRecord.recitationsTime
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

    fun getPrayerNames(): Array<String> =
        resources.getStringArray(R.array.prayer_names)

}