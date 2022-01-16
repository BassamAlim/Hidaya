package bassamalim.hidaya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.databinding.ActivityAboutBinding;
import bassamalim.hidaya.other.Global;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListener();
    }

    private void setListener() {
        binding.rebuildDb.setOnClickListener(v -> {
            deleteDatabase("HidayaDB");

            Log.i(Global.TAG, "Database Rebuilt");

            reviveDb();

            Toast.makeText(this, "تمت إعادة بناء قاعدة البيانات",
                    Toast.LENGTH_SHORT).show();
        });
        binding.driveUpdate.setOnClickListener(v -> {
            String url = FirebaseRemoteConfig.getInstance().getString(Global.UPDATE_URL);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

    private void reviveDb() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        deleteDatabase("HidayaDB");

        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        String surasJson = pref.getString("favorite_suras", "");
        String recitersJson = pref.getString("favorite_reciters", "");
        String athkarJson = pref.getString("favorite_athkar", "");

        Gson gson = new Gson();

        if (surasJson.length() != 0) {
            Object[] favSuras = gson.fromJson(surasJson, Object[].class);
            for (int i = 0; i < favSuras.length; i++) {
                Double d = (Double) favSuras[i];
                db.suraDao().setFav(i, d.intValue());
            }
        }

        if (recitersJson.length() != 0) {
            Object[] favReciters = gson.fromJson(recitersJson, Object[].class);
            for (int i = 0; i < favReciters.length; i++) {
                Double d = (Double) favReciters[i];
                db.telawatRecitersDao().setFav(i, d.intValue());
            }
        }

        if (athkarJson.length() != 0) {
            Object[] favAthkar = gson.fromJson(athkarJson, Object[].class);
            for (int i = 0; i < favAthkar.length; i++) {
                Double d = (Double) favAthkar[i];
                db.athkarDao().setFav(i, d.intValue());
            }
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("last_db_version", Global.dbVer);
        editor.apply();

        Log.i(Global.TAG, "Database Revived");
    }

}
