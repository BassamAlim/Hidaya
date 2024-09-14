package bassamalim.hidaya.core.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.room.Room
import bassamalim.hidaya.core.data.room.AppDatabase
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.AppStatePreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.BooksPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.RecitationsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.RemembrancePreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.migrations.AppSettingsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.AppStatePreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.BooksPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.NotificationsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.PrayersPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.QuranPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.RecitationsPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.RemembrancesPreferencesMigration
import bassamalim.hidaya.core.data.preferences.migrations.UserPreferencesMigration
import bassamalim.hidaya.core.data.preferences.objects.AppSettingsPreferences
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences
import bassamalim.hidaya.core.data.preferences.objects.BooksPreferences
import bassamalim.hidaya.core.data.preferences.objects.NotificationsPreferences
import bassamalim.hidaya.core.data.preferences.objects.PrayersPreferences
import bassamalim.hidaya.core.data.preferences.objects.QuranPreferences
import bassamalim.hidaya.core.data.preferences.objects.RecitationsPreferences
import bassamalim.hidaya.core.data.preferences.objects.RemembrancesPreferences
import bassamalim.hidaya.core.data.preferences.objects.UserPreferences
import bassamalim.hidaya.core.data.preferences.serializers.AppSettingsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.AppStatePreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.BooksPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.NotificationsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.PrayersPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.QuranPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.RecitationsPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.RemembrancesPreferencesSerializer
import bassamalim.hidaya.core.data.preferences.serializers.UserPreferencesSerializer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides @Singleton
    fun provideResources(application: Application): Resources =
        application.resources

    @Provides @Singleton
    fun provideAppSettingsPreferencesDataSource(@ApplicationContext appContext: Context) =
        AppSettingsPreferencesDataSource(
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
    fun provideAppStatePreferencesDataSource(@ApplicationContext appContext: Context) =
        AppStatePreferencesDataSource(
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
    fun provideBooksPreferencesDataSource(@ApplicationContext appContext: Context) =
        BooksPreferencesDataSource(
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
    fun provideNotificationsPreferencesDataSource(@ApplicationContext appContext: Context) =
        NotificationsPreferencesDataSource(
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
    fun providePrayersPreferencesDataSource(@ApplicationContext appContext: Context) =
        PrayersPreferencesDataSource(
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
    fun provideQuranPreferencesDataSource(@ApplicationContext appContext: Context) =
        QuranPreferencesDataSource(
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
    fun provideRecitationsPreferencesDataSource(@ApplicationContext appContext: Context) =
        RecitationsPreferencesDataSource(
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
    fun provideRemembrancesPreferencesDataSource(@ApplicationContext appContext: Context) =
        RemembrancePreferencesDataSource(
            DataStoreFactory.create(
                serializer = RemembrancesPreferencesSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { RemembrancesPreferences() }
                ),
                migrations = listOf(RemembrancesPreferencesMigration.getMigration(appContext)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { appContext.dataStoreFile("remembrances_preferences") }
            )
        )

    @Provides @Singleton
    fun provideUserPreferencesDataSource(@ApplicationContext appContext: Context) =
        UserPreferencesDataSource(
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

    @Singleton @Provides
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context = context.applicationContext,
            klass = AppDatabase::class.java,
            name = "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db").build()

//    @Provides @Singleton
//    fun provideDatabase(application: Application) =
//        Room.databaseBuilder(
//            application, AppDatabase::class.java, "HidayaDB"
//        ).createFromAsset("databases/HidayaDB.db")
//            .allowMainThreadQueries()
//            .build()

    @Provides
    fun provideBooksDao(database: AppDatabase) = database.booksDao()

    @Provides
    fun provideCitiesDao(database: AppDatabase) = database.citiesDao()

    @Provides
    fun provideCountriesDao(database: AppDatabase) = database.countriesDao()

    @Provides
    fun provideQuizAnswersDao(database: AppDatabase) = database.quizAnswersDao()

    @Provides
    fun provideQuizQuestionsDao(database: AppDatabase) = database.quizQuestionsDao()

    @Provides
    fun provideRecitationRecitersDao(database: AppDatabase) = database.recitationRecitersDao()

    @Provides
    fun provideRecitationNarrationsDao(database: AppDatabase) = database.recitationNarrationsDao()

    @Provides
    fun provideRemembranceCategoriesDao(database: AppDatabase) = database.remembranceCategoriesDao()

    @Provides
    fun provideRemembrancePassagesDao(database: AppDatabase) = database.remembrancePassagesDao()

    @Provides
    fun provideRemembrancesDao(database: AppDatabase) = database.remembrancesDao()

    @Provides
    fun provideSurasDao(database: AppDatabase) = database.surasDao()

    @Provides
    fun provideVerseRecitationsDao(database: AppDatabase) = database.verseRecitationsDao()

    @Provides
    fun provideVerseRecitersDao(database: AppDatabase) = database.verseRecitersDao()

    @Provides
    fun provideVersesDao(database: AppDatabase) = database.versesDao()

    @Provides @Singleton
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideRemoteConfig() = FirebaseRemoteConfig.getInstance()

}