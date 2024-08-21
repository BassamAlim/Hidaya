package bassamalim.hidaya.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import bassamalim.hidaya.core.data.database.daos.BooksDao
import bassamalim.hidaya.core.data.database.daos.CitiesDao
import bassamalim.hidaya.core.data.database.daos.CountriesDao
import bassamalim.hidaya.core.data.database.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.database.daos.QuizQuestionsDao
import bassamalim.hidaya.core.data.database.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.database.daos.RecitationNarrationsDao
import bassamalim.hidaya.core.data.database.daos.RecitationsDao
import bassamalim.hidaya.core.data.database.daos.RemembranceCategoriesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancePassagesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancesDao
import bassamalim.hidaya.core.data.database.daos.SurasDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitersDao
import bassamalim.hidaya.core.data.database.daos.VersesDao
import bassamalim.hidaya.core.data.database.models.Book
import bassamalim.hidaya.core.data.database.models.City
import bassamalim.hidaya.core.data.database.models.Country
import bassamalim.hidaya.core.data.database.models.QuizAnswer
import bassamalim.hidaya.core.data.database.models.QuizQuestion
import bassamalim.hidaya.core.data.database.models.RecitationsReciter
import bassamalim.hidaya.core.data.database.models.RecitationNarrations
import bassamalim.hidaya.core.data.database.models.Remembrance
import bassamalim.hidaya.core.data.database.models.RemembranceCategory
import bassamalim.hidaya.core.data.database.models.RemembrancePassage
import bassamalim.hidaya.core.data.database.models.Sura
import bassamalim.hidaya.core.data.database.models.Verse
import bassamalim.hidaya.core.data.database.models.VerseRecitation
import bassamalim.hidaya.core.data.database.models.VerseReciter

@Database(
    entities = [
        Book::class,
        City::class,
        Country::class,
        QuizAnswer::class,
        QuizQuestion::class,
        RecitationsReciter::class,
        RecitationNarrations::class,
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
    abstract fun recitationsDao(): RecitationsDao
    abstract fun recitationVersionsDao(): RecitationNarrationsDao
    abstract fun remembranceCategoriesDao(): RemembranceCategoriesDao
    abstract fun remembrancePassagesDao(): RemembrancePassagesDao
    abstract fun remembrancesDao(): RemembrancesDao
    abstract fun surasDao(): SurasDao
    abstract fun verseRecitationsDao(): VerseRecitationsDao
    abstract fun verseRecitersDao(): VerseRecitersDao
    abstract fun versesDao(): VersesDao
}