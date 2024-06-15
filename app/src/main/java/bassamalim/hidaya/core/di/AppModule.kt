package bassamalim.hidaya.core.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.about.AboutRepo
import bassamalim.hidaya.features.athkarList.AthkarListRepo
import bassamalim.hidaya.features.athkarViewer.AthkarViewerRepo
import bassamalim.hidaya.features.bookChapters.BookChaptersRepo
import bassamalim.hidaya.features.bookSearcher.BookSearcherRepo
import bassamalim.hidaya.features.bookViewer.BookViewerRepo
import bassamalim.hidaya.features.books.BooksRepo
import bassamalim.hidaya.features.dateConverter.DateConverterRepo
import bassamalim.hidaya.features.hijriDatePicker.HijriDatePickerRepo
import bassamalim.hidaya.features.home.HomeRepo
import bassamalim.hidaya.features.leaderboard.LeaderboardRepo
import bassamalim.hidaya.features.locationPicker.LocationPickerRepo
import bassamalim.hidaya.features.locator.LocatorRepo
import bassamalim.hidaya.features.main.MainRepo
import bassamalim.hidaya.features.prayerReminder.PrayerReminderRepo
import bassamalim.hidaya.features.prayerSetting.PrayerSettingsRepo
import bassamalim.hidaya.features.prayers.PrayersRepo
import bassamalim.hidaya.features.qibla.QiblaRepo
import bassamalim.hidaya.features.quiz.QuizRepo
import bassamalim.hidaya.features.quizResult.QuizResultRepo
import bassamalim.hidaya.features.quran.QuranRepo
import bassamalim.hidaya.features.quranSearcher.QuranSearcherRepo
import bassamalim.hidaya.features.quranSettings.QuranSettingsRepo
import bassamalim.hidaya.features.quranViewer.QuranViewerRepo
import bassamalim.hidaya.features.radio.RadioClientRepo
import bassamalim.hidaya.features.settings.SettingsRepo
import bassamalim.hidaya.features.telawat.TelawatRepo
import bassamalim.hidaya.features.telawatPlayer.TelawatClientRepo
import bassamalim.hidaya.features.telawatSuar.TelawatSuarRepo
import bassamalim.hidaya.features.tv.TvRepo
import bassamalim.hidaya.features.welcome.WelcomeRepo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
    fun provideResources(application: Application): Resources =
        application.resources


    @Provides @Singleton
    fun providePreferencesDataSource(application: Application) =
        PreferencesDataSource(PreferenceManager.getDefaultSharedPreferences(application))

    @Provides @Singleton
    fun provideDatabase(application: Application) =
        Room.databaseBuilder(
            application, AppDatabase::class.java, "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries()
            .build()

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
        preferencesDataSource: PreferencesDataSource
    ) = AboutRepo(preferencesDataSource)

    @Provides @Singleton
    fun provideAthkarListRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        gson: Gson
    ) = AthkarListRepo(preferencesDataSource, database, gson)

    @Provides @Singleton
    fun provideAthkarViewerRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = AthkarViewerRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideBookChaptersRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
        gson: Gson
    ) = BookChaptersRepo(context, preferencesDataSource, gson)

    @Provides @Singleton
    fun provideBookSearcherRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        gson: Gson
    ) = BookSearcherRepo(context, preferencesDataSource, database, gson)

    @Provides @Singleton
    fun provideBooksRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        gson: Gson
    ) = BooksRepo(context, preferencesDataSource, database, gson)

    @Provides @Singleton
    fun provideBookViewerRepository(
        context: Context,
        preferencesDataSource: PreferencesDataSource,
        gson: Gson
    ) = BookViewerRepo(context, preferencesDataSource, gson)

    @Provides @Singleton
    fun provideDateConverterRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = DateConverterRepo(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideHomeRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        firestore: FirebaseFirestore
    ) = HomeRepo(resources, preferencesDataSource, database, firestore)

    @Provides @Singleton
    fun provideLeaderboardRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        firestore: FirebaseFirestore
    ) = LeaderboardRepo(resources, preferencesDataSource, firestore)

    @Provides @Singleton
    fun provideLocationPickerRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = LocationPickerRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideLocatorRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = LocatorRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideMainRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = MainRepo(resources, preferencesDataSource)

    @Provides @Singleton
    fun providePrayersRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = PrayersRepo(resources, preferencesDataSource, database)

    @Provides @Singleton
    fun providePrayerReminderRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = PrayerReminderRepo(resources, preferencesDataSource)

    @Provides @Singleton
    fun providePrayerSettingsRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = PrayerSettingsRepo(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideQiblaRepository(
        preferencesDataSource: PreferencesDataSource
    ) = QiblaRepo(preferencesDataSource)

    @Provides @Singleton
    fun provideQuizRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuizRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuizResultRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuizResultRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        gson: Gson
    ) = QuranRepo(resources, preferencesDataSource, database, gson)

    @Provides @Singleton
    fun provideQuranSearcherRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranSearcherRepo(resources, preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranSettingsRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranSettingsRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideQuranViewerRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = QuranViewerRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideRadioClientRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = RadioClientRepo(remoteConfig)

    @Provides @Singleton
    fun provideSettingsRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = SettingsRepo(resources, preferencesDataSource)

    @Provides @Singleton
    fun provideTelawatClientRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase
    ) = TelawatClientRepo(preferencesDataSource, database)

    @Provides @Singleton
    fun provideTelawatRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        gson: Gson
    ) = TelawatRepo(resources, preferencesDataSource, database, gson)

    @Provides @Singleton
    fun provideTelawatSuarRepository(
        preferencesDataSource: PreferencesDataSource,
        database: AppDatabase,
        gson: Gson
    ) = TelawatSuarRepo(preferencesDataSource, database, gson)

    @Provides @Singleton
    fun provideTvRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = TvRepo(remoteConfig)

    @Provides @Singleton
    fun provideWelcomeRepository(
        preferencesDataSource: PreferencesDataSource
    ) = WelcomeRepo(preferencesDataSource)

    @Provides @Singleton
    fun provideHijriDatePickerRepository(
        resources: Resources,
        preferencesDataSource: PreferencesDataSource
    ) = HijriDatePickerRepo(resources, preferencesDataSource)

}