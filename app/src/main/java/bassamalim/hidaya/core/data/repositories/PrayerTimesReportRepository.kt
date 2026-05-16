package bassamalim.hidaya.core.data.repositories

import android.app.Application
import android.util.Log
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.utils.OsUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PrayerTimesReportRepository @Inject constructor(
    private val app: Application,
    private val firestore: FirebaseFirestore
) {

    suspend fun submitReport(report: Map<String, Any?>): Boolean {
        if (!OsUtils.isNetworkAvailable(app)) return false

        return try {
            firestore.collection("PrayerTimesReports")
                .add(report + ("created_at" to System.currentTimeMillis()))
                .await()
            true
        } catch (e: Exception) {
            Log.e(Globals.TAG, "Failed to submit prayer times report: $e")
            false
        }
    }

}
