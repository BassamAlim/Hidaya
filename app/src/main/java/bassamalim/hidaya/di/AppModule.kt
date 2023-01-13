package bassamalim.hidaya.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.repository.*
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
    fun provideAthkarRepository() = AthkarRepo()

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
    fun provideHomeRepository() = HomeRepo()

    @Provides @Singleton
    fun provideLocatorRepository() = LocatorRepo()

    @Provides @Singleton
    fun provideMoreRepository() = MoreRepo()

    @Provides @Singleton
    fun providePrayersRepository() = PrayersRepo()

    @Provides @Singleton
    fun provideQiblaRepository() = QiblaRepo()

    @Provides @Singleton
    fun provideQuizLobbyRepository() = QuizLobbyRepo()

    @Provides @Singleton
    fun provideQuizRepository() = QuizRepo()

    @Provides @Singleton
    fun provideQuizResultRepository() = QuizResultRepo()

    @Provides @Singleton
    fun provideQuranRepository() = QuranRepo()

    @Provides @Singleton
    fun provideQuranSearcherRepository() = QuranSearcherRepo()

    @Provides @Singleton
    fun provideQuranViewerRepository() = QuranViewerRepo()

    @Provides @Singleton
    fun provideRadioClientRepository() = RadioClientRepo()

    @Provides @Singleton
    fun provideSettingsRepository() = SettingsRepo()

    @Provides @Singleton
    fun provideTelawatClientRepository() = TelawatClientRepo()

    @Provides @Singleton
    fun provideTelawatRepository() = TelawatRepo()

    @Provides @Singleton
    fun provideTelawatSuarRepository() = TelawatSuarRepo()

    @Provides @Singleton
    fun provideTvRepository() = TvRepo()

    @Provides @Singleton
    fun provideWelcomeRepository(
        preferences: SharedPreferences
    ) = WelcomeRepo(preferences)

}