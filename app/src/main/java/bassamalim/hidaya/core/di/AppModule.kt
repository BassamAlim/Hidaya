package bassamalim.hidaya.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.features.about.AboutRepo
import bassamalim.hidaya.features.athkarList.AthkarListRepo
import bassamalim.hidaya.features.athkarViewer.AthkarViewerRepo
import bassamalim.hidaya.features.bookChapters.BookChaptersRepo
import bassamalim.hidaya.features.bookSearcher.BookSearcherRepo
import bassamalim.hidaya.features.bookViewer.BookViewerRepo
import bassamalim.hidaya.features.books.BooksRepo
import bassamalim.hidaya.features.dateConverter.DateConverterRepo
import bassamalim.hidaya.features.home.HomeRepo
import bassamalim.hidaya.features.locationPicker.LocationPickerRepo
import bassamalim.hidaya.features.locator.LocatorRepo
import bassamalim.hidaya.features.main.MainRepo
import bassamalim.hidaya.features.prayers.PrayersRepo
import bassamalim.hidaya.features.qibla.QiblaRepo
import bassamalim.hidaya.features.quiz.QuizRepo
import bassamalim.hidaya.features.quizResult.QuizResultRepo
import bassamalim.hidaya.features.quran.QuranRepo
import bassamalim.hidaya.features.quranSearcher.QuranSearcherRepo
import bassamalim.hidaya.features.quranViewer.QuranViewerRepo
import bassamalim.hidaya.features.radio.RadioClientRepo
import bassamalim.hidaya.features.settings.SettingsRepo
import bassamalim.hidaya.features.tv.TvRepo
import bassamalim.hidaya.features.telawat.TelawatRepo
import bassamalim.hidaya.features.telawatClient.TelawatClientRepo
import bassamalim.hidaya.features.telawatSuar.TelawatSuarRepo
import bassamalim.hidaya.features.welcome.WelcomeRepo
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
    fun providePreferences(application: Application) =
        PreferenceManager.getDefaultSharedPreferences(application)!!

    @Provides @Singleton
    fun provideDatabase(application: Application) =
        Room.databaseBuilder(
            application, AppDatabase::class.java, "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries()
            .build()

    @Provides @Singleton
    fun provideRemoteConfig() = FirebaseRemoteConfig.getInstance()

    @Provides @Singleton
    fun provideGson() = Gson()


    @Provides @Singleton
    fun provideAboutRepository(
        pref: SharedPreferences
    ) = AboutRepo(pref)

    @Provides @Singleton
    fun provideAthkarListRepository(
        application: Application,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = AthkarListRepo(application, preferences, database, gson)

    @Provides @Singleton
    fun provideAthkarViewerRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = AthkarViewerRepo(preferences, database)

    @Provides @Singleton
    fun provideBookChaptersRepository(
        context: Context,
        preferences: SharedPreferences,
        gson: Gson
    ) = BookChaptersRepo(context, preferences, gson)

    @Provides @Singleton
    fun provideBookSearcherRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = BookSearcherRepo(context, preferences, database, gson)

    @Provides @Singleton
    fun provideBooksRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = BooksRepo(context, preferences, database, gson)

    @Provides @Singleton
    fun provideBookViewerRepository(
        context: Context,
        preferences: SharedPreferences,
        gson: Gson
    ) = BookViewerRepo(context, preferences, gson)

    @Provides @Singleton
    fun provideDateConverterRepository(
        resources: Resources,
        preferences: SharedPreferences
    ) = DateConverterRepo(resources, preferences)

    @Provides @Singleton
    fun provideHomeRepository(
        resources: Resources,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = HomeRepo(resources, preferences, database)

    @Provides @Singleton
    fun provideLocationPickerRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = LocationPickerRepo(preferences, database)

    @Provides @Singleton
    fun provideLocatorRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = LocatorRepo(preferences, database)

    @Provides @Singleton
    fun provideMainRepository(
        resources: Resources,
        preferences: SharedPreferences
    ) = MainRepo(resources, preferences)

    @Provides @Singleton
    fun providePrayersRepository(
        resources: Resources,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = PrayersRepo(resources, preferences, database)

    @Provides @Singleton
    fun provideQiblaRepository(
        preferences: SharedPreferences
    ) = QiblaRepo(preferences)

    @Provides @Singleton
    fun provideQuizRepository(
        resources: Resources,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuizRepo(resources, preferences, database)

    @Provides @Singleton
    fun provideQuizResultRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuizResultRepo(preferences, database)

    @Provides @Singleton
    fun provideQuranRepository(
        resources: Resources,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = QuranRepo(resources, preferences, database, gson)

    @Provides @Singleton
    fun provideQuranSearcherRepository(
        resources: Resources,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuranSearcherRepo(resources, preferences, database)

    @Provides @Singleton
    fun provideQuranViewerRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuranViewerRepo(preferences, database)

    @Provides @Singleton
    fun provideRadioClientRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = RadioClientRepo(remoteConfig)

    @Provides @Singleton
    fun provideSettingsRepository(
        resources: Resources,
        preferences: SharedPreferences
    ) = SettingsRepo(resources, preferences)

    @Provides @Singleton
    fun provideTelawatClientRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = TelawatClientRepo(preferences, database)

    @Provides @Singleton
    fun provideTelawatRepository(
        resources: Resources,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = TelawatRepo(resources, preferences, database, gson)

    @Provides @Singleton
    fun provideTelawatSuarRepository(
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = TelawatSuarRepo(preferences, database, gson)

    @Provides @Singleton
    fun provideTvRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = TvRepo(remoteConfig)

    @Provides @Singleton
    fun provideWelcomeRepository(
        preferences: SharedPreferences
    ) = WelcomeRepo(preferences)

}