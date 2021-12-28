package bassamalim.hidaya.other;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import bassamalim.hidaya.interfaces.AyahDao;
import bassamalim.hidaya.interfaces.SuraDao;
import bassamalim.hidaya.models.JAyah;
import bassamalim.hidaya.models.SuraDB;

@Database(entities = {JAyah.class, SuraDB.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AyahDao ayahDao();

    public abstract SuraDao suraDao();

}
