package bassamalim.hidaya.database

import androidx.room.Database
import androidx.room.RoomDatabase
import bassamalim.hidaya.database.daos.*
import bassamalim.hidaya.database.dbs.*

@Database(
    entities = [AyatDB::class, SuarDB::class, AthkarCategoryDB::class, AthkarDB::class,
        ThikrsDB::class, AyatRecitersDB::class, AyatTelawaDB::class, TelawatRecitersDB::class,
        TelawatVersionsDB::class, QuizQuestionsDB::class, QuizAnswersDB::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ayahDao(): AyatDao
    abstract fun suarDao(): SuarDao
    abstract fun athkarCategoryDao(): AthkarCategoryDao
    abstract fun athkarDao(): AthkarDao
    abstract fun thikrsDao(): ThikrsDao
    abstract fun ayatRecitersDao(): AyatRecitersDao
    abstract fun ayatTelawaDao(): AyatTelawaDao
    abstract fun telawatRecitersDao(): TelawatRecitersDao
    abstract fun telawatVersionsDao(): TelawatVersionsDao
    abstract fun telawatDao(): TelawatDao
    abstract fun quizQuestionDao(): QuizQuestionsDao
    abstract fun quizAnswerDao(): QuizAnswersDao
}