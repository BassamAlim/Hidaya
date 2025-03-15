package bassamalim.hidaya.core.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import bassamalim.hidaya.core.data.dataSources.room.daos.SurasDao
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.widgets.PrayersWidget
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DbUtils
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Random
import javax.inject.Inject

@AndroidEntryPoint
class DailyUpdateReceiver : BroadcastReceiver() {

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var appStateRepository: AppStateRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var recitationsRepository: RecitationsRepository
    @Inject lateinit var remembrancesRepository: RemembrancesRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var surasDao: SurasDao
    @Inject lateinit var alarm: Alarm
    @Inject @IoDispatcher lateinit var dispatcher: CoroutineDispatcher

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Globals.TAG, "in DailyUpdateReceiver")

        testDb(context)
        GlobalScope.launch {
            ActivityUtils.configure(
                context = context,
                applicationContext = context.applicationContext,
                language = appSettingsRepository.getLanguage().first(),
            )

            val now = Calendar.getInstance()
            if ((intent.action == "daily" && notUpdatedToday(now)) || intent.action == "boot") {
                val location = locationRepository.getLocation().first() ?: return@launch
                when (location.type) {
                    LocationType.AUTO -> locate(context, now)
                    LocationType.MANUAL -> update(context = context, location = null, now = now)
                    LocationType.NONE -> return@launch
                }

                pickWerd()
            }
            else Log.i(Globals.TAG, "dead intent in daily update receiver")

            setTomorrow(context)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun testDb(context: Context) {
        GlobalScope.launch {
            val shouldReviveDb = DbUtils.shouldReviveDb(
                lastDbVersion = appStateRepository.getLastDbVersion().first(),
                test = surasDao::getPlainNamesAr,
                dispatcher = dispatcher
            )
            if (shouldReviveDb) {
                reviveDb(context)
                appStateRepository.setLastDbVersion(Globals.DB_VERSION)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun reviveDb(context: Context) {
        DbUtils.resetDB(context)
        GlobalScope.launch {
            DbUtils.restoreDbData(
                suraFavorites = quranRepository.getSuraFavoritesBackup().first(),
                setSuraFavorites = quranRepository::setSuraFavorites,
                reciterFavorites = recitationsRepository.getReciterFavoritesBackup().first(),
                setReciterFavorites = recitationsRepository::setReciterFavorites,
                remembranceFavorites = remembrancesRepository.getFavoritesBackup().first(),
                setRemembranceFavorites = remembrancesRepository::setFavorites,
            )
        }
    }

    private suspend fun notUpdatedToday(now: Calendar): Boolean {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.timeInMillis = appStateRepository.getLastDailyUpdateMillis().first()
        return lastUpdate[Calendar.DATE] != now[Calendar.DATE]
    }

    private fun locate(context: Context, now: Calendar) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.addOnSuccessListener {
                    location: Location? ->
                    update(context = context, location = location, now = now)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun update(context: Context, location: Location?, now: Calendar) {
        GlobalScope.launch {
            if (location != null)
                locationRepository.setLocation(location)

            val latestLocation = locationRepository.getLocation().first()
            if (latestLocation == null) {
                Log.e(Globals.TAG, "no available location in DailyUpdate")
                return@launch
            }

            val prayerTimes = PrayerTimeUtils.getPrayerTimes(
                settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
                selectedTimeZoneId = locationRepository.getTimeZone(latestLocation.ids.cityId),
                location = latestLocation,
                calendar = now
            )

            alarm.setAll(prayerTimes)

            updateWidget(context)

            setUpdated(now)
        }
    }

    private fun updateWidget(context: Context) {
        val intent = Intent(context, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager
            .getInstance(context.applicationContext)
            .getAppWidgetIds(ComponentName(context.applicationContext, PrayersWidget::class.java))

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(intent)
    }

    private fun setUpdated(now: Calendar) {
        appStateRepository.setLastDailyUpdateMillis(now.timeInMillis)
    }

    private fun pickWerd() {
        val randomWerd = Random().nextInt(Globals.NUM_OF_QURAN_PAGES)
        quranRepository.setWerdPageNum(randomWerd)
        quranRepository.setWerdDone(false)
    }

    private fun setTomorrow(context: Context) {
        val intent = Intent(context.applicationContext, DailyUpdateReceiver::class.java)
        intent.action = "daily"

        val time = Calendar.getInstance()
        time[Calendar.DATE]++
        time[Calendar.HOUR_OF_DAY] = Globals.DAILY_UPDATE_HOUR
        time[Calendar.MINUTE] = Globals.DAILY_UPDATE_MINUTE

        val pendIntent = PendingIntent.getBroadcast(
            context.applicationContext, 1210, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.applicationContext
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendIntent)
    }

}