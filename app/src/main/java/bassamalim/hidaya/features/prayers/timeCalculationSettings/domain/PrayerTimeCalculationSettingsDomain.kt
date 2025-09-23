package bassamalim.hidaya.features.prayers.timeCalculationSettings.domain

import android.content.Context
import android.content.Intent
import android.os.Build
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.services.PrayersNotificationService
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class PrayerTimeCalculationSettingsDomain @Inject constructor(
    private val prayersRepository: PrayersRepository,
    private val locationRepository: LocationRepository,
    private val alarm: Alarm
) {

    fun getContinuousPrayersNotificationEnabled() =
        prayersRepository.getContinuousPrayersNotificationEnabled()

    fun setContinuousPrayersNotificationEnabled(enabled: Boolean, context: Context) {
        prayersRepository.setContinuousPrayersNotificationEnabled(enabled)

        if (enabled) {
            val serviceIntent = Intent(context, PrayersNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(serviceIntent)
            else
                context.startService(serviceIntent)
        }
        else {
            context.stopService(Intent(context, PrayersNotificationService::class.java))
        }
    }

    fun getPrayerTimesCalculatorSettings() = prayersRepository.getPrayerTimesCalculatorSettings()

    fun setPrayerTimesCalculatorSettings(settings: PrayerTimeCalculatorSettings) {
        prayersRepository.setPrayerTimesCalculatorSettings(settings)
    }

    suspend fun resetPrayerTimes() {
        val location = locationRepository.getLocation().first() ?: return

        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )

        alarm.setAll(prayerTimes)
    }

}