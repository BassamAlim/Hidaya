package bassamalim.hidaya.core.receivers

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.widgets.PrayersWidget
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Use EntryPointAccessors to retrieve dependencies
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PrayersWidgetReceiverEntryPoint::class.java
        )

        appSettingsRepository = hiltEntryPoint.appSettingsRepository()
        prayersRepository = hiltEntryPoint.prayersRepository()
        locationRepository = hiltEntryPoint.locationRepository()
        dispatcher = hiltEntryPoint.dispatcher()
    }

}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PrayersWidgetReceiverEntryPoint {
    fun appSettingsRepository(): AppSettingsRepository
    fun prayersRepository(): PrayersRepository
    fun locationRepository(): LocationRepository
    @IoDispatcher fun dispatcher(): CoroutineDispatcher
}