package bassamalim.hidaya.core.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class PrayersWidget(
    private val appSettingsRepository: AppSettingsRepository,
    private val prayersRepository: PrayersRepository,
    private val locationRepository: LocationRepository,
    private val dispatcher: CoroutineDispatcher
) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prayerTimeStrings = withContext(dispatcher) {
            bootstrapApp(context)

            getPrayerTimeStringMap(context)
        }

        provideContent {
            GlanceTheme {
                WidgetContent(prayerTimeStrings)
            }
        }
    }

    @Composable
    private fun WidgetContent(prayerTimesStrings: Map<Prayer, String>?) {
        if (prayerTimesStrings == null) {
            Text(
                text = stringResource(R.string.error_fetching_data),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            )
        }
        else {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.surface),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (prayer in prayerTimesStrings) {
                    Text(
                        text = prayer.value,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }

    private suspend fun bootstrapApp(context: Context) {
        ActivityUtils.configure(
            context = context,
            applicationContext = context.applicationContext,
            language = appSettingsRepository.getLanguage().first()
        )
    }

    private suspend fun getPrayerTimeStringMap(context: Context): Map<Prayer, String>? {
        val location = locationRepository.getLocation().first<Location?>() ?: return null
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

        val strings = mutableMapOf<Prayer, String>()
        for (n in prayerNames.indices.reversed()) {
            if (n == 1) continue  // To skip sunrise

            val name = prayerNames[n]
            val prayer = prayerTimeStrings.keys.elementAt(n)
            val timeString = prayerTimeStrings[prayer]

            strings[prayer] = "$name\n$timeString"
        }
        return strings
    }

}