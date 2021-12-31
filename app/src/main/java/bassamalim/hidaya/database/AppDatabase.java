package bassamalim.hidaya.database;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {AyatDB.class, SuraDB.class, AthkarCategoryDB.class, AthkarDB.class,
                ThikrsDB.class, AyatRecitersDB.class, AyatTelawaDB.class, TelawatRecitersDB.class,
                TelawatVersionsDB.class},
        version = 1/*, autoMigrations = {@AutoMigration(from = 1, to = 2)}*/
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AyatDao ayahDao();

    public abstract SuraDao suraDao();

    public abstract AthkarCategoryDao athkarCategoryDao();

    public abstract AthkarDao athkarDao();

    public abstract ThikrsDao thikrsDao();

    public abstract AyatRecitersDao ayatRecitersDao();

    public abstract AyatTelawaDao ayatTelawaDao();

    public abstract TelawatRecitersDao telawatRecitersDao();

    public abstract TelawatVersionsDao telawatVersionsDao();

    public abstract TelawatDao telawatDao();

}
