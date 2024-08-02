package bassamalim.hidaya.core.data.repositories

import android.util.Log
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.database.daos.CityDao
import bassamalim.hidaya.core.data.database.daos.CountryDao
import bassamalim.hidaya.core.data.database.dbs.CityDB
import bassamalim.hidaya.core.data.database.dbs.CountryDB
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.other.Global
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val appSettingsPrefsDataSource: AppSettingsPreferencesDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val firestore: FirebaseFirestore,
    private val countryDao: CountryDao,
    private val cityDao: CityDao
) {

    fun getLocation() = userPreferencesDataSource.flow.map {
        it.location
    }
    suspend fun setLocation(location: Location) {
        userPreferencesDataSource.update { it.copy(
            location = location
        )}
    }

    fun getLocalRecord() = userPreferencesDataSource.flow.map {
        it.userRecord
    }
    suspend fun setLocalRecord(userRecord: UserRecord) {
        userPreferencesDataSource.update { it.copy(
            userRecord = userRecord
        )}
    }

    fun getTimeZone(cityId: Int) = cityDao.getCity(cityId).timeZone

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

    suspend fun getCountries(): List<CountryDB> {
        val language = appSettingsPrefsDataSource.flow.map {
            it.language
        }.first()

        return countryDao.getAll().sortedBy { countryDB: CountryDB ->
            if (language == Language.ENGLISH) countryDB.nameEn
            else countryDB.nameAr
        }
    }

    suspend fun getCities(countryId: Int): List<CityDB> {
        val language = appSettingsPrefsDataSource.flow.map {
            it.language
        }.first()

        return if (language == Language.ENGLISH)
            cityDao.getTopEn(countryId, "").toList()
        else
            cityDao.getTopAr(countryId, "").toList()
    }

    fun getCity(cityId: Int) = cityDao.getCity(cityId)

    suspend fun setLocation(countryId: Int, cityId: Int) {
        val city = getCity(cityId)

        userPreferencesDataSource.update { it.copy(
            location = Location(
                type = LocationType.Manual,
                latitude = city.latitude,
                longitude = city.longitude,
                countryId = countryId,
                cityId = cityId
            )
        )}
    }

}