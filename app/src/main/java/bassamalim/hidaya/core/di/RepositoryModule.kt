package bassamalim.hidaya.core.di

import android.app.Application
import android.content.res.Resources
import bassamalim.hidaya.core.data.database.daos.BooksDao
import bassamalim.hidaya.core.data.database.daos.CitiesDao
import bassamalim.hidaya.core.data.database.daos.CountriesDao
import bassamalim.hidaya.core.data.database.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.database.daos.QuizQuestionsDao
import bassamalim.hidaya.core.data.database.daos.RecitationNarrationsDao
import bassamalim.hidaya.core.data.database.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.database.daos.RemembranceCategoriesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancePassagesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancesDao
import bassamalim.hidaya.core.data.database.daos.SurasDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitersDao
import bassamalim.hidaya.core.data.database.daos.VersesDao
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.AppStatePreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.BooksPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.NotificationsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.QuranPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.RecitationsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.RemembrancePreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideAppSettingsRepository(
        appSettingsPreferencesDataSource: AppSettingsPreferencesDataSource
    ) = AppSettingsRepository(appSettingsPreferencesDataSource)

    @Provides @Singleton
    fun provideAppStateRepository(
        resources: Resources,
        appStatePreferencesDataSource: AppStatePreferencesDataSource
    ) = AppStateRepository(resources, appStatePreferencesDataSource)

    @Provides @Singleton
    fun provideBooksRepository(
        app: Application,
        resources: Resources,
        booksDao: BooksDao,
        booksPreferencesDataSource: BooksPreferencesDataSource,
        gson: Gson
    ) = BooksRepository(app, resources, booksDao, booksPreferencesDataSource, gson)

    @Provides @Singleton
    fun provideLiveContentRepository(
        remoteConfig: FirebaseRemoteConfig
    ) = LiveContentRepository(remoteConfig)

    @Provides @Singleton
    fun provideLocationRepository(
        userPreferencesDataSource: UserPreferencesDataSource,
        countriesDao: CountriesDao,
        citiesDao: CitiesDao
    ) = LocationRepository(userPreferencesDataSource, countriesDao, citiesDao)

    @Provides @Singleton
    fun provideNotificationsRepository(
        notificationsPreferencesDataSource: NotificationsPreferencesDataSource
    ) = NotificationsRepository(notificationsPreferencesDataSource)

    @Provides @Singleton
    fun providePrayersRepository(
        resources: Resources,
        prayersPreferencesDataSource: PrayersPreferencesDataSource
    ) = PrayersRepository(resources, prayersPreferencesDataSource)

    @Provides @Singleton
    fun provideQuizRepository(
        quizQuestionsDao: QuizQuestionsDao,
        quizAnswersDao: QuizAnswersDao
    ) = QuizRepository(quizQuestionsDao, quizAnswersDao)

    @Provides @Singleton
    fun provideQuranRepository(
        quranPreferencesDataSource: QuranPreferencesDataSource,
        surasDao: SurasDao,
        versesDao: VersesDao
    ) = QuranRepository(quranPreferencesDataSource, surasDao, versesDao)

    @Provides @Singleton
    fun provideRecitationsRepository(
        app: Application,
        recitationsPreferencesDataSource: RecitationsPreferencesDataSource,
        recitationRecitersDao: RecitationRecitersDao,
        verseRecitationsDao: VerseRecitationsDao,
        verseRecitersDao: VerseRecitersDao,
        recitationNarrationsDao: RecitationNarrationsDao
    ) = RecitationsRepository(
        app,
        recitationsPreferencesDataSource,
        recitationRecitersDao,
        verseRecitationsDao,
        verseRecitersDao,
        recitationNarrationsDao
    )

    @Provides @Singleton
    fun provideRemembrancesRepository(
        remembrancePreferencesDataSource: RemembrancePreferencesDataSource,
        remembranceCategoriesDao: RemembranceCategoriesDao,
        remembrancesDao: RemembrancesDao,
        remembrancePassagesDao: RemembrancePassagesDao
    ) = RemembrancesRepository(
        remembrancePreferencesDataSource,
        remembranceCategoriesDao,
        remembrancesDao,
        remembrancePassagesDao
    )

    @Provides @Singleton
    fun provideUserRepository(
        userPreferencesDataSource: UserPreferencesDataSource,
        firestore: FirebaseFirestore
    ) = UserRepository(userPreferencesDataSource, firestore)

}