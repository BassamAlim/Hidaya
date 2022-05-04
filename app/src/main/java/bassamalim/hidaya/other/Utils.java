package bassamalim.hidaya.other;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.helpers.PrayTimes;

public class Utils {

    public static String onActivityCreateSetTheme(Activity activity) {
        String theme = PreferenceManager.getDefaultSharedPreferences(activity).getString(
                activity.getString(R.string.theme_key), activity.getString(R.string.default_theme));
        if (theme.equals("ThemeM"))
            activity.setTheme(R.style.Theme_HidayaM);
        return theme;
    }

    public static void refresh(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }

    public static String onActivityCreateSetLocale(Context context, String language) {
        if (language == null)
            language = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(R.string.language_key), "ar");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return language;
    }

    public static String translateNumbers(Context context, String english) {
        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(
                R.string.language_key), context.getString(R.string.default_language)).equals("ar"))
            return english;

        HashMap<Character, Character> map = new HashMap<>();
        map.put('0', '٠');
        map.put('1', '١');
        map.put('2', '٢');
        map.put('3', '٣');
        map.put('4', '٤');
        map.put('5', '٥');
        map.put('6', '٦');
        map.put('7', '٧');
        map.put('8', '٨');
        map.put('9', '٩');
        map.put('A', 'ص');
        map.put('P', 'م');

        if (english.charAt(0) == '0') {
            english = english.replaceFirst("0", "");
            if (english.charAt(0) == '0')
                english = english.replaceFirst("0:", "");
        }

        StringBuilder temp = new StringBuilder();
        for (int j = 0; j < english.length(); j++) {
            char t = english.charAt(j);
            if (map.containsKey(t))
                t = map.get(t);
            temp.append(t);
        }

        return temp.toString();
    }

    public static Calendar[] getTimes(Context context, Location loc) {
        Calendar calendar = Calendar.getInstance();

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(calendar.getTime().getTime());
        double timezone = millis / 3600000.0;

        return new PrayTimes(context).getPrayerTimesArray(calendar, loc.getLatitude(),
                loc.getLongitude(), timezone);
    }

    public static void reviveDb(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        context.deleteDatabase("HidayaDB");

        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB")
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

    public static boolean createDir(Context context, String postfix) {
        File dir = new File(context.getExternalFilesDir(null) + postfix);

        if (!dir.exists())
            return dir.mkdirs();
        else
            return false;
    }

    public static boolean deleteFile(Context context, String postfix) {
        File file = new File(context.getExternalFilesDir(null) + postfix);

        if (file.exists())
            return file.delete();
        else
            return false;
    }

    public static void cancelAlarm(Context gContext, ID id) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(gContext, id.ordinal(),
                new Intent(), PendingIntent.FLAG_CANCEL_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) gContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);

        Log.i(Global.TAG, "Canceled Alarm " + id);
    }

    public static ID mapID(int num) {
        switch (num) {
            case 0: return ID.FAJR;
            case 1: return ID.SHOROUQ;
            case 2: return ID.DUHR;
            case 3: return ID.ASR;
            case 4: return ID.MAGHRIB;
            case 5: return ID.ISHAA;
            case 6: return ID.MORNING;
            case 7: return ID.EVENING;
            case 8: return ID.DAILY_WERD;
            case 9: return ID.FRIDAY_KAHF;
            default: return null;
        }
    }

    public static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    public static String getJsonFromDownloads(String path) {
        String jsonStr = "";

        FileInputStream fin = null;
        try {
            File file = new File(path);
            fin = new FileInputStream(file);

            FileChannel fc = fin.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            jsonStr = Charset.defaultCharset().decode(bb).toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jsonStr;
    }

}
