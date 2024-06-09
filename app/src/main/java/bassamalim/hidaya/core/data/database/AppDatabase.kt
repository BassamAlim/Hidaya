package bassamalim.hidaya.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import bassamalim.hidaya.core.data.database.daos.AthkarCategoryDao
import bassamalim.hidaya.core.data.database.daos.AthkarDao
import bassamalim.hidaya.core.data.database.daos.AthkarPartsDao
import bassamalim.hidaya.core.data.database.daos.AyatDao
import bassamalim.hidaya.core.data.database.daos.AyatRecitersDao
import bassamalim.hidaya.core.data.database.daos.AyatTelawaDao
import bassamalim.hidaya.core.data.database.daos.BooksDao
import bassamalim.hidaya.core.data.database.daos.CityDao
import bassamalim.hidaya.core.data.database.daos.CountryDao
import bassamalim.hidaya.core.data.database.daos.QuizAnswersDao
import bassamalim.hidaya.core.data.database.daos.QuizQuestionsDao
import bassamalim.hidaya.core.data.database.daos.SuarDao
import bassamalim.hidaya.core.data.database.daos.TelawatDao
import bassamalim.hidaya.core.data.database.daos.TelawatRecitersDao
import bassamalim.hidaya.core.data.database.daos.TelawatRewayatDao
import bassamalim.hidaya.core.data.database.dbs.AthkarCategoryDB
import bassamalim.hidaya.core.data.database.dbs.AthkarDB
import bassamalim.hidaya.core.data.database.dbs.AthkarPartsDB
import bassamalim.hidaya.core.data.database.dbs.AyatDB
import bassamalim.hidaya.core.data.database.dbs.AyatRecitersDB
import bassamalim.hidaya.core.data.database.dbs.AyatTelawaDB
import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.data.database.dbs.CityDB
import bassamalim.hidaya.core.data.database.dbs.CountryDB
import bassamalim.hidaya.core.data.database.dbs.QuizAnswersDB
import bassamalim.hidaya.core.data.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.core.data.database.dbs.SuarDB
import bassamalim.hidaya.core.data.database.dbs.TelawatRecitersDB
import bassamalim.hidaya.core.data.database.dbs.TelawatRewayatDB

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