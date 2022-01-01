package bassamalim.hidaya.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import bassamalim.hidaya.database.daos.AthkarCategoryDao;
import bassamalim.hidaya.database.daos.AthkarDao;
import bassamalim.hidaya.database.daos.AyatDao;
import bassamalim.hidaya.database.daos.AyatRecitersDao;
import bassamalim.hidaya.database.daos.AyatTelawaDao;
import bassamalim.hidaya.database.daos.SuraDao;
import bassamalim.hidaya.database.daos.TelawatDao;
import bassamalim.hidaya.database.daos.TelawatRecitersDao;
import bassamalim.hidaya.database.daos.TelawatVersionsDao;
import bassamalim.hidaya.database.daos.ThikrsDao;
import bassamalim.hidaya.database.dbs.AthkarCategoryDB;
import bassamalim.hidaya.database.dbs.AthkarDB;
import bassamalim.hidaya.database.dbs.AyatDB;
import bassamalim.hidaya.database.dbs.AyatRecitersDB;
import bassamalim.hidaya.database.dbs.AyatTelawaDB;
import bassamalim.hidaya.database.dbs.SuraDB;
import bassamalim.hidaya.database.dbs.TelawatRecitersDB;
import bassamalim.hidaya.database.dbs.TelawatVersionsDB;
import bassamalim.hidaya.database.dbs.ThikrsDB;

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
