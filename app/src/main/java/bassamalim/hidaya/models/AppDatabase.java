package bassamalim.hidaya.models;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {JAyah.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AyahDao ayahDao();
}
