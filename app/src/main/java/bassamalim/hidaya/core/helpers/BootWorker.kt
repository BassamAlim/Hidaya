package bassamalim.hidaya.core.helpers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.services.PrayersNotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BootWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val prayersRepository: PrayersRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prayersNotificationEnabled =
            prayersRepository.getContinuousPrayersNotificationEnabled().first()

        if (prayersNotificationEnabled) {
            val serviceIntent = Intent(applicationContext, PrayersNotificationService::class.java)
            try {
                ContextCompat.startForegroundService(applicationContext, serviceIntent)
            } catch (e: SecurityException) {
                Log.w("BootWorker", "Cannot start foreground service from background: ${e.message}")
                // Service will handle the ForegroundServiceStartNotAllowedException internally
            }
        }

        return Result.success()
    }
}
