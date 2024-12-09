package bassamalim.hidaya.core.other

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.dataSources.room.daos.SurasDao
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DbUtils
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.SortedMap
import javax.inject.Inject

@AndroidEntryPoint
class PrayersWidget : AppWidgetProvider() {

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var appStateRepository: AppStateRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var recitationsRepository: RecitationsRepository
    @Inject lateinit var remembrancesRepository: RemembrancesRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var surasDao: SurasDao
    @Inject @IoDispatcher lateinit var dispatcher: CoroutineDispatcher

    @OptIn(DelicateCoroutinesApi::class)
    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        testDb(context)
        GlobalScope.launch {
            bootstrapApp(context)
        }

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private suspend fun bootstrapApp(context: Context) {
        ActivityUtils.configure(
            context = context,
            applicationContext = context.applicationContext,
            language = appSettingsRepository.getLanguage().first(),
            theme = appSettingsRepository.getTheme().first(),
        )
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
                appStateRepository.setLastDbVersion(Global.DB_VERSION)
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        GlobalScope.launch {
            val prayerTimeStrings = getPrayerTimeStringMap(context) ?: return@launch

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_prayers)

            views.setTextViewText(R.id.widget_fajr, prayerTimeStrings[Prayer.FAJR])
            views.setTextViewText(R.id.widget_dhuhr, prayerTimeStrings[Prayer.DHUHR])
            views.setTextViewText(R.id.widget_asr, prayerTimeStrings[Prayer.ASR])
            views.setTextViewText(R.id.widget_maghrib, prayerTimeStrings[Prayer.MAGHRIB])
            views.setTextViewText(R.id.widget_ishaa, prayerTimeStrings[Prayer.ISHAA])

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private suspend fun getPrayerTimeStringMap(context: Context): SortedMap<Prayer, String>? {
        val location = locationRepository.getLocation().first() ?: return null

        val prayerTimes = PrayerTimeUtils.getPrayerTimes(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            selectedTimeZoneId = locationRepository.getTimeZone(location.ids.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )
        val prayerTimeStrings = PrayerTimeUtils.formatPrayerTimes(
            prayerTimes = prayerTimes,
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )

        val prayerNames = context.resources.getStringArray(R.array.prayer_names)

        val strings = sortedMapOf<Prayer, String>()
        for (n in prayerNames.indices) {
            if (n == 1) continue  // To skip sunrise

            val name = prayerNames[n]
            val prayer = prayerTimeStrings.keys.elementAt(n)
            val timeString = prayerTimeStrings[prayer]

            strings[prayer] = "$name\n$timeString"
        }
        return strings
    }

}