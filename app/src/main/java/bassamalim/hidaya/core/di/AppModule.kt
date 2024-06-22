package bassamalim.hidaya.core.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.migrations.AppSettingsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.AppStatePreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.BooksPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.NotificationsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.PrayersPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.QuranPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.RecitationsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.SupplicationsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.UserPreferencesMigration
import bassamalim.hidaya.core.data.preferences.objects.AppSettingsPreferences
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences
import bassamalim.hidaya.core.data.preferences.objects.BooksPreferences
import bassamalim.hidaya.core.data.preferences.objects.NotificationsPreferences
import bassamalim.hidaya.core.data.preferences.objects.PrayersPreferences
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import bassamalim.hidaya.core.data.preferences.objects.SupplicationsPreferences
import bassamalim.hidaya.core.data.preferences.objects.UserPreferences
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.AppStatePreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.NotificationsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.PrayersPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.QuranPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.RecitationsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.SupplicationsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.UserPreferencesRepository
import bassamalim.hidaya.core.data.preferences.serializers.AppSettingsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.AppStatePreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.BooksPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.NotificationsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.PrayersPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.QuranPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.RecitationsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.SupplicationsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.UserPreferencesSerializer
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.about.data.AboutRepository
import bassamalim.hidaya.features.supplicationsMenu.SupplicationsMenuRepository
import bassamalim.hidaya.features.supplicationsReader.SupplicationsReaderRepository
import bassamalim.hidaya.features.bookChapters.data.BookChaptersRepository
import bassamalim.hidaya.features.bookSearcher.BookSearcherRepository
import bassamalim.hidaya.features.bookReader.BookReaderRepository
import bassamalim.hidaya.features.books.BooksRepository
import bassamalim.hidaya.features.dateConverter.DateConverterRepository
import bassamalim.hidaya.features.hijriDatePicker.HijriDatePickerRepository
import bassamalim.hidaya.features.home.HomeRepository
import bassamalim.hidaya.features.leaderboard.LeaderboardRepository
import bassamalim.hidaya.features.locationPicker.LocationPickerRepository
import bassamalim.hidaya.features.locator.LocatorRepository
import bassamalim.hidaya.features.main.MainRepository
import bassamalim.hidaya.features.prayerReminder.PrayerReminderRepository
import bassamalim.hidaya.features.prayerSetting.PrayerSettingsRepository
import bassamalim.hidaya.features.prayers.PrayersRepository
import bassamalim.hidaya.features.qibla.QiblaRepository
import bassamalim.hidaya.features.quiz.QuizRepository
import bassamalim.hidaya.features.quizResult.QuizResultRepository
import bassamalim.hidaya.features.quran.QuranRepository
import bassamalim.hidaya.features.quranSearcher.QuranSearcherRepository
import bassamalim.hidaya.features.quranSettings.QuranSettingsRepository
import bassamalim.hidaya.features.quranReader.QuranReaderRepository
import bassamalim.hidaya.features.radio.RadioClientRepository
import bassamalim.hidaya.features.settings.SettingsRepository
import bassamalim.hidaya.features.recitationsRecitersMenu.RecitationsRecitersMenuRepository
import bassamalim.hidaya.features.recitationsPlayer.RecitationsPlayerClientRepository
import bassamalim.hidaya.features.recitationsSuarMenu.RecitationsSuarRepository
import bassamalim.hidaya.features.tv.TvRepository
import bassamalim.hidaya.features.onboarding.OnboardingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // Sets how long does the dependencies live
object AppModule {

    @Provides @Singleton  // Sets how many instances of this dependency can be created
    fun provideApplicationContext(application: Application) =
        application.applicationContext!!

    @Provides @Singleton
    fun provideResources(application: Application): Resources =
        application.resources

    @Provides @Singleton
    fun provideDatabase(application: Application) =
        Room.databaseBuilder(
            application, AppDatabase::class.java, "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries()
            .build()

    @Provides @Singleton
    fun provideAppSettingsPreferencesRepository(@ApplicationContext appContext: Context) =
        AppSettingsPreferencesRepository(
            DataStoreFactory.create(
                serializer = AppSettingsPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { AppSettingsPreferences() }
                ),
                migrations = listOf(AppSettingsPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("app_settings_preferences") }
            )
        )

    @Provides @Singleton
    fun provideAppStatePreferencesRepository(@ApplicationContext appContext: Context) =
        AppStatePreferencesRepository(
            DataStoreFactory.create(
                serializer = AppStatePreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { AppStatePreferences() }
                ),
                migrations = listOf(AppStatePreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("app_state_preferences") }
            )
        )

    @Provides @Singleton
    fun provideBooksPreferencesRepository(@ApplicationContext appContext: Context) =
        BooksPreferencesRepository(
            DataStoreFactory.create(
                serializer = BooksPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { BooksPreferences() }
                ),
                migrations = listOf(BooksPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("books_preferences") }
            )
        )

    @Provides @Singleton
    fun provideNotificationsPreferencesRepository(@ApplicationContext appContext: Context) =
        NotificationsPreferencesRepository(
            DataStoreFactory.create(
                serializer = NotificationsPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { NotificationsPreferences() }
                ),
                migrations = listOf(NotificationsPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("notifications_preferences") }
            )
        )

    @Provides @Singleton
    fun providePrayersPreferencesRepository(@ApplicationContext appContext: Context) =
        PrayersPreferencesRepository(
            DataStoreFactory.create(
                serializer = PrayersPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { PrayersPreferences() }
                ),
                migrations = listOf(PrayersPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("prayers_preferences") }
            )
        )

    @Provides @Singleton
    fun provideQuranPreferencesRepository(@ApplicationContext appContext: Context) =
        QuranPreferencesRepository(
            DataStoreFactory.create(
                serializer = QuranPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { QuranPreferences() }
                ),
                migrations = listOf(QuranPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("quran_preferences") }
            )
        )

    @Provides @Singleton
    fun provideRecitationsPreferencesRepository(@ApplicationContext appContext: Context) =
        RecitationsPreferencesRepository(
            DataStoreFactory.create(
                serializer = RecitationsPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { RecitationsPreferences() }
                ),
                migrations = listOf(RecitationsPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("recitations_preferences") }
            )
        )

    @Provides @Singleton
    fun provideSupplicationsPreferencesRepository(@ApplicationContext appContext: Context) =
        SupplicationsPreferencesRepository(
            DataStoreFactory.create(
                serializer = SupplicationsPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { SupplicationsPreferences() }
                ),
                migrations = listOf(SupplicationsPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("supplications_preferences") }
            )
        )

    @Provides @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext appContext: Context) =
        UserPreferencesRepository(
            DataStoreFactory.create(
                serializer = UserPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { UserPreferences() }
                ),
                migrations = listOf(UserPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("user_preferences") }
            )
        )

    @Provides @Singleton
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideRemoteConfig() = FirebaseRemoteConfig.getInstance()

    @Provides @Singleton
    fun provideGson() = Gson()

    @Provides @Singleton
    fun provideNavigator() = Navigator()


    @Provides @Singleton
    fun provideAboutRepository(
        appStatePreferencesRepository: AppStatePreferencesRepository
    ) = AboutRepository(appStatePreferencesRepository)

    @Provides @Singleton
    fun provideBookChaptersRepository(
        context: Context,
        gson: Gson,
        appSettingsPreferencesRepository: AppSettingsPreferencesRepository,
        booksPreferencesRepository: BooksPreferencesRepository,
    ) = BookChaptersRepository(
        context,
        gson,
        appSettingsPreferencesRepository,
        booksPreferencesRepository
    )

    @Provides @Singleton
    fun provideBookReaderRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
    ) = BookReaderRepository(context, preferencesDataSource)

    @Provides @Singleton
    fun provideBooksRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
    ) = BooksRepository(context, preferencesDataSource, database)

    @Provides @Singleton

`    fun provideBookSearcherRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
    ) = BookSearcherRepository(context, preferencesDataSource, database)

    @Provides @Singleton
    fun provideDateConverterRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = DateConverterRepository(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideHijriDatePickerRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = HijriDatePickerRepository(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideHomeRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        firestore: FirebaseFirestore
    ) = HomeRepository(resources, preferencesDataSource, database, firestore)

    @Provides @Singleton
    fun provideLeaderboardRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        firestore: FirebaseFirestore
    ) = LeaderboardRepository(resources, preferencesDataSource, firestore)

    @Provides @Singleton
    fun provideLocationPickerRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = LocationPickerRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideLocatorRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = LocatorRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideMainRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = MainRepository(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideOnboardingRepository(
        preferencesDataSource: PreferencesDataSource
    ) = OnboardingRepository(preferencesDataSource)

    @Provides @Singleton
    fun providePrayerReminderRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = PrayerReminderRepository(resources, preferencesDataSource)

    @Provides @Singleton
    fun providePrayersRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = PrayersRepository(resources, preferencesDataSource, database)

    @Provides @Singleton
    fun providePrayerSettingsRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = PrayerSettingsRepository(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideQiblaRepository(
        preferencesDataSource: PreferencesDataSource
    ) = QiblaRepository(preferencesDataSource)

    @Provides @Singleton
    fun provideQuizRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuizRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuizResultRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuizResultRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranRepository(resources, preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranReaderRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranReaderRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranSearcherRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranSearcherRepository(resources, preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranSettingsRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranSettingsRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideRadioClientRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = RadioClientRepository(remoteConfig)

    @Provides @Singleton
    fun provideRecitationsPlayerClientRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = RecitationsPlayerClientRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideRecitationsRecitersMenuRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = RecitationsRecitersMenuRepository(resources, preferencesDataSource, database)

    @Provides @Singleton
    fun provideRecitationsSuarRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = RecitationsSuarRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideSettingsRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = SettingsRepository(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideSupplicationsMenuRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
    ) = SupplicationsMenuRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideSupplicationsReaderRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = SupplicationsReaderRepository(preferencesDataSource, database)

    @Provides @Singleton
    fun provideTvRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = TvRepository(remoteConfig)

}