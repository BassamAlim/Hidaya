package bassamalim.hidaya.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import bassamalim.hidaya.core.data.database.daos.*
import bassamalim.hidaya.core.data.database.dbs.*

@Database(
    entities = [AyatDB::class, SuarDB::class, BooksDB::class,
        AthkarCategoryDB::class, AthkarDB::class, AthkarPartsDB::class,
        AyatRecitersDB::class, AyatTelawaDB::class, TelawatRecitersDB::class,
        TelawatRewayatDB::class, QuizQuestionsDB::class, QuizAnswersDB::class,
               CountryDB::class, CityDB::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ayatDao(): AyatDao
    abstract fun suarDao(): SuarDao
    abstract fun booksDao(): BooksDao
    abstract fun athkarCategoryDao(): AthkarCategoryDao
    abstract fun athkarDao(): AthkarDao
    abstract fun athkarPartsDao(): AthkarPartsDao
    abstract fun ayatRecitersDao(): AyatRecitersDao
    abstract fun ayatTelawaDao(): AyatTelawaDao
    abstract fun telawatRecitersDao(): TelawatRecitersDao
    abstract fun telawatRewayatDao(): TelawatRewayatDao
    abstract fun telawatDao(): TelawatDao
    abstract fun quizQuestionDao(): QuizQuestionsDao
    abstract fun quizAnswerDao(): QuizAnswersDao
    abstract fun countryDao(): CountryDao
    abstract fun cityDao(): CityDao
}