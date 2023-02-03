package bassamalim.hidaya.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.repository.*
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

    @Provides @Singleton
    fun providesApplicationContext(application: Application): Context =
        application.applicationContext

    @Provides @Singleton  // Sets how many instances of this dependency can be created
    fun provideDatabase(application: Application) =
        Room.databaseBuilder(
            application.applicationContext, AppDatabase::class.java, "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries()
            .build()

    @Provides @Singleton
    fun providePreferences(application: Application) =
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)!!

    // try it and see if you can change language
//    @Provides @Singleton
//    fun provideResources(application: Application) =
//        application.applicationContext.resources

    fun provideRemoteConfig() = FirebaseRemoteConfig.getInstance()

    @Provides @Singleton
    fun provideGson() = Gson()


    @Provides @Singleton
    fun provideAboutRepository(pref: SharedPreferences) = AboutRepo(pref)

    @Provides @Singleton
    fun provideAthkarListRepository(
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = AthkarListRepo(preferences, database, gson)

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
        context: Context,
        preferences: SharedPreferences
    ) = DateConverterRepo(context, preferences)

    @Provides @Singleton
    fun provideHomeRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = HomeRepo(context, preferences, database)

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
        context: Context,
        preferences: SharedPreferences
    ) = MainRepo(context, preferences)

    @Provides @Singleton
    fun providePrayersRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = PrayersRepo(context, preferences, database)

    @Provides @Singleton
    fun provideQiblaRepository(
        preferences: SharedPreferences
    ) = QiblaRepo(preferences)

    @Provides @Singleton
    fun provideQuizRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuizRepo(context, preferences, database)

    @Provides @Singleton
    fun provideQuizResultRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuizResultRepo(preferences, database)

    @Provides @Singleton
    fun provideQuranRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = QuranRepo(context, preferences, database, gson)

    @Provides @Singleton
    fun provideQuranSearcherRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuranSearcherRepo(context, preferences, database)

    @Provides @Singleton
    fun provideQuranViewerRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = QuranViewerRepo(preferences, database)

    @Provides @Singleton
    fun provideRadioClientRepository(
        context: Context,
        remoteConfig: FirebaseRemoteConfig
    ) = RadioClientRepo(context, remoteConfig)

    @Provides @Singleton
    fun provideSettingsRepository(
        context: Context,
        preferences: SharedPreferences
    ) = SettingsRepo(context, preferences)

    @Provides @Singleton
    fun provideSplashRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = SplashRepo(preferences, database)

    @Provides @Singleton
    fun provideTelawatClientRepository(
        preferences: SharedPreferences,
        database: AppDatabase
    ) = TelawatClientRepo(preferences, database)

    @Provides @Singleton
    fun provideTelawatRepository(
        context: Context,
        preferences: SharedPreferences,
        database: AppDatabase,
        gson: Gson
    ) = TelawatRepo(context, preferences, database, gson)

    @Provides @Singleton
    fun provideTelawatSuarRepository() = TelawatSuarRepo()

    @Provides @Singleton
    fun provideTvRepository() = TvRepo()

    @Provides @Singleton
    fun provideWelcomeRepository(
        preferences: SharedPreferences
    ) = WelcomeRepo(preferences)

}