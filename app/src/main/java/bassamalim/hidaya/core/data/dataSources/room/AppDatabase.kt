package bassamalim.hidaya.core.data.dataSources.room

import androidx.room.Database
import androidx.room.RoomDatabase
import bassamalim.hidaya.core.data.dataSources.room.daos.BooksDao
import bassamalim.hidaya.core.data.dataSources.room.daos.CitiesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.CountriesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.dataSources.room.daos.QuizQuestionsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RecitationNarrationsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RemembranceCategoriesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RemembrancePassagesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RemembrancesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.SurasDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VerseRecitersDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VersesDao
import bassamalim.hidaya.core.data.dataSources.room.entities.Book
import bassamalim.hidaya.core.data.dataSources.room.entities.City
import bassamalim.hidaya.core.data.dataSources.room.entities.Country
import bassamalim.hidaya.core.data.dataSources.room.entities.QuizAnswer
import bassamalim.hidaya.core.data.dataSources.room.entities.QuizQuestion
import bassamalim.hidaya.core.data.dataSources.room.entities.RecitationNarration
import bassamalim.hidaya.core.data.dataSources.room.entities.RecitationsReciter
import bassamalim.hidaya.core.data.dataSources.room.entities.Remembrance
import bassamalim.hidaya.core.data.dataSources.room.entities.RemembranceCategory
import bassamalim.hidaya.core.data.dataSources.room.entities.RemembrancePassage
import bassamalim.hidaya.core.data.dataSources.room.entities.Sura
import bassamalim.hidaya.core.data.dataSources.room.entities.Verse
import bassamalim.hidaya.core.data.dataSources.room.entities.VerseRecitation
import bassamalim.hidaya.core.data.dataSources.room.entities.VerseReciter

@Database(
    entities = [
        Book::class,
        City::class,
        Country::class,
        QuizAnswer::class,
        QuizQuestion::class,
        RecitationsReciter::class,
        RecitationNarration::class,
        RemembranceCategory::class,
        RemembrancePassage::class,
        Remembrance::class,
        Sura::class,
        VerseRecitation::class,
        VerseReciter::class,
        Verse::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun booksDao(): BooksDao
    abstract fun citiesDao(): CitiesDao
    abstract fun countriesDao(): CountriesDao
    abstract fun quizAnswersDao(): QuizAnswersDao
    abstract fun quizQuestionsDao(): QuizQuestionsDao
    abstract fun recitationRecitersDao(): RecitationRecitersDao
    abstract fun recitationNarrationsDao(): RecitationNarrationsDao
    abstract fun remembranceCategoriesDao(): RemembranceCategoriesDao
    abstract fun remembrancePassagesDao(): RemembrancePassagesDao
    abstract fun remembrancesDao(): RemembrancesDao
    abstract fun surasDao(): SurasDao
    abstract fun verseRecitationsDao(): VerseRecitationsDao
    abstract fun verseRecitersDao(): VerseRecitersDao
    abstract fun versesDao(): VersesDao
}