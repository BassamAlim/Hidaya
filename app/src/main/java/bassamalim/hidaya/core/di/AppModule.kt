package bassamalim.hidaya.core.di

import android.app.Application
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.core.helpers.Navigator
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // Sets how long does the dependencies live
object AppModule {

    @Provides @Singleton  // Sets how many instances of this dependency can be created
    fun provideApplicationContext(application: Application) =
        application.applicationContext!!

    @Provides @Singleton
    fun provideGson() = Gson()

    @Provides @Singleton
    fun provideNavigator() = Navigator()

    @Provides @Singleton
    fun provideAlarm(
        app: Application,
        prayersRepository: PrayersRepository,
        notificationsRepository: NotificationsRepository,
        locationRepository: LocationRepository
    ) = Alarm(app, prayersRepository, notificationsRepository, locationRepository)

}