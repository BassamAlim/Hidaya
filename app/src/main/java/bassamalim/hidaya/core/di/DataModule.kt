package bassamalim.hidaya.core.di

import android.content.Context
import androidx.room.Room
import bassamalim.hidaya.core.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton @Provides
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context = context.applicationContext,
            klass = AppDatabase::class.java,
            name = "HidayaDB"
        ).createFromAsset("databases/HidayaDB.db").build()

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
    fun provideRecitationsDao(database: AppDatabase) = database.recitationsDao()

    @Provides
    fun provideRecitationVersionsDao(database: AppDatabase) = database.recitationVersionsDao()

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

}