package bassamalim.hidaya.core.receivers

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.widgets.PrayersWidget
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

@AndroidEntryPoint
class PrayersWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject @IoDispatcher lateinit var dispatcher: CoroutineDispatcher

    override val glanceAppWidget: GlanceAppWidget
        get() = PrayersWidget(
            appSettingsRepository = appSettingsRepository,
            prayersRepository = prayersRepository,
            locationRepository = locationRepository,
            dispatcher = dispatcher
        )

}