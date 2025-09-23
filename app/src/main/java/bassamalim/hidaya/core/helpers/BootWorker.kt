package bassamalim.hidaya.core.helpers

import android.content.Context
import android.content.Intent
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
            ContextCompat.startForegroundService(applicationContext, serviceIntent)
        }

        return Result.success()
    }
}
