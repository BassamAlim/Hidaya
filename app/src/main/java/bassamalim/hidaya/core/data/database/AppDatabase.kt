package bassamalim.hidaya.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import bassamalim.hidaya.core.data.database.daos.RemembranceCategoriesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancePassagesDao
import bassamalim.hidaya.core.data.database.daos.VersesDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitersDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.database.daos.BooksDao
import bassamalim.hidaya.core.data.database.daos.CityDao
import bassamalim.hidaya.core.data.database.daos.CountryDao
import bassamalim.hidaya.core.data.database.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.database.daos.QuizQuestionsDao
import bassamalim.hidaya.core.data.database.daos.SurasDao
import bassamalim.hidaya.core.data.database.daos.RecitationsDao
import bassamalim.hidaya.core.data.database.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.database.daos.RecitationVersionsDao
import bassamalim.hidaya.core.data.database.models.RemembranceCategory
import bassamalim.hidaya.core.data.database.models.Remembrance
import bassamalim.hidaya.core.data.database.models.RemembrancePassage
import bassamalim.hidaya.core.data.database.models.Verse
import bassamalim.hidaya.core.data.database.models.VerseReciter
import bassamalim.hidaya.core.data.database.models.VerseRecitation
import bassamalim.hidaya.core.data.database.models.Book
import bassamalim.hidaya.core.data.database.models.City
import bassamalim.hidaya.core.data.database.models.Country
import bassamalim.hidaya.core.data.database.models.QuizAnswer
import bassamalim.hidaya.core.data.database.models.QuizQuestion
import bassamalim.hidaya.core.data.database.models.Sura
import bassamalim.hidaya.core.data.database.models.RecitationsReciter
import bassamalim.hidaya.core.data.database.models.RecitationsVersion

@Database(
    entities = [Verse::class, Sura::class, Book::class,
        RemembranceCategory::class, Remembrance::class, RemembrancePassage::class,
        VerseReciter::class, VerseRecitation::class, RecitationsReciter::class,
        RecitationsVersion::class, QuizQuestion::class, QuizAnswer::class,
               Country::class, City::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ayatDao(): VersesDao
    abstract fun suarDao(): SurasDao
    abstract fun booksDao(): BooksDao
    abstract fun athkarCategoryDao(): RemembranceCategoriesDao
    abstract fun athkarDao(): RemembrancesDao
    abstract fun athkarPartsDao(): RemembrancePassagesDao
    abstract fun ayatRecitersDao(): VerseRecitersDao
    abstract fun ayatTelawaDao(): VerseRecitationsDao
    abstract fun telawatRecitersDao(): RecitationRecitersDao
    abstract fun telawatRewayatDao(): RecitationVersionsDao
    abstract fun telawatDao(): RecitationsDao
    abstract fun quizQuestionDao(): QuizQuestionsDao
    abstract fun quizAnswerDao(): QuizAnswersDao
    abstract fun countryDao(): CountryDao
    abstract fun cityDao(): CityDao
}