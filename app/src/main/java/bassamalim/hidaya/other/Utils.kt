package bassamalim.hidaya.other

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.ID
import bassamalim.hidaya.helpers.PrayTimes
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

object Utils {

    fun myOnActivityCreated(activity: Activity) {
        onActivityCreateSetTheme(activity)
        onActivityCreateSetLocale(activity)
    }

    fun onActivityCreateSetTheme(activity: Activity): String {
        val theme: String? = PreferenceManager.getDefaultSharedPreferences(activity)
                .getString(activity.getString(R.string.theme_key), activity.getString(R.string.default_theme))
        when(theme) {
            "ThemeM" -> activity.setTheme(R.style.Theme_HidayaM)
            "ThemeR" -> activity.setTheme(R.style.Theme_HidayaN)
            else -> activity.setTheme(R.style.Theme_HidayaL)
        }
        return theme!!
    }

    fun onActivityCreateSetLocale(context: Context): String {
        val language: String? = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.language_key), "ar")

        val locale = Locale(language!!)
        Locale.setDefault(locale)
        val resources = context.resources

        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return language
    }

    fun refresh(activity: Activity) {
        val intent: Intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

    fun translateNumbers(context: Context, english: String, timeFormat: Boolean): String {
        var eng = english
        if (timeFormat) {
            if (eng.startsWith('0')) {
                eng = eng.replaceFirst("0", "")
                if (eng.startsWith('0')) {
                    eng = eng.replaceFirst("0:", "")
                    if (eng.startsWith('0') && !eng.startsWith("00"))
                        eng = eng.replaceFirst("0", "")
                }
            }
        }

        if (!PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.numerals_language_key), context.getString(R.string.default_language)
            ).equals("ar"))
            return eng

        val map = HashMap<Char, Char>()
        map['0'] = '٠'
        map['1'] = '١'
        map['2'] = '٢'
        map['3'] = '٣'
        map['4'] = '٤'
        map['5'] = '٥'
        map['6'] = '٦'
        map['7'] = '٧'
        map['8'] = '٨'
        map['9'] = '٩'
        map['a'] = 'ص'
        map['p'] = 'م'

        val temp = StringBuilder()
        for (char in eng) {
            if (map.containsKey(char)) temp.append(map[char])
            else if (char != 'm') temp.append(char)
        }

        return temp.toString()
    }

    fun formatTime(str: String) {

    }

    fun getTimes(context: Context, loc: Location?): Array<Calendar?> {
        val calendar = Calendar.getInstance()

        val timeZoneObj = TimeZone.getDefault()
        val millis = timeZoneObj.getOffset(calendar.time.time).toLong()
        val timezone = millis / 3600000.0

        return PrayTimes(context).getPrayerTimes(
            loc!!.latitude, loc.longitude, timezone, calendar
        )
    }

    fun reviveDb(context: Context) {
        val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        context.deleteDatabase("HidayaDB")

        val db: AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java,
            "HidayaDB").createFromAsset("databases/HidayaDB.db")
            .allowMainThreadQueries().build()

        val surasJson: String? = pref.getString("favorite_suras", "")
        val recitersJson: String? = pref.getString("favorite_reciters", "")
        val athkarJson: String? = pref.getString("favorite_athkar", "")

        val gson = Gson()

        if (surasJson!!.isNotEmpty()) {
            val favSuras: Array<Any> = gson.fromJson(surasJson, Array<Any>::class.java)
            for (i in favSuras.indices) {
                val d = favSuras[i] as Double
                db.suarDao().setFav(i, d.toInt())
            }
        }

        if (recitersJson!!.isNotEmpty()) {
            val favReciters: Array<Any> = gson.fromJson(recitersJson, Array<Any>::class.java)
            for (i in favReciters.indices) {
                val d = favReciters[i] as Double
                db.telawatRecitersDao().setFav(i, d.toInt())
            }
        }

        if (athkarJson!!.isNotEmpty()) {
            val favAthkar: Array<Any> = gson.fromJson(athkarJson, Array<Any>::class.java)
            for (i in favAthkar.indices) {
                val d = favAthkar[i] as Double
                db.athkarDao().setFav(i, d.toInt())
            }
        }

        val editor: SharedPreferences.Editor = pref.edit()
        editor.putInt("last_db_version", Global.dbVer)
        editor.apply()

        Log.i(Global.TAG, "Database Revived")
    }

    fun createDir(context: Context, postfix: String): Boolean {
        val dir = File(context.getExternalFilesDir(null).toString() + postfix)

        return if (!dir.exists()) dir.mkdirs() else false
    }

    fun deleteFile(context: Context, postfix: String): Boolean {
        val file = File(context.getExternalFilesDir(null).toString() + postfix)

        return if (file.exists()) file.delete() else false
    }

    fun cancelAlarm(gContext: Context, id: ID) {
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            gContext, id.ordinal, Intent(),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am: AlarmManager = gContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)

        Log.i(Global.TAG, "Canceled Alarm $id")
    }

    fun mapID(num: Int): ID? {
        return when (num) {
            0 -> ID.FAJR
            1 -> ID.SHOROUQ
            2 -> ID.DUHR
            3 -> ID.ASR
            4 -> ID.MAGHRIB
            5 -> ID.ISHAA
            6 -> ID.MORNING
            7 -> ID.EVENING
            8 -> ID.DAILY_WERD
            9 -> ID.FRIDAY_KAHF
            else -> null
        }
    }

    fun getJsonFromAssets(context: Context, fileName: String?): String? {
        val jsonString: String = try {
            val `is` = context.assets.open(fileName!!)

            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()

            String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return jsonString
    }

    fun getJsonFromDownloads(path: String): String {
        var jsonStr = ""

        var fin: FileInputStream? = null
        try {
            val file = File(path)
            fin = FileInputStream(file)

            val fc = fin.channel
            val bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())

            jsonStr = Charset.defaultCharset().decode(bb).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fin!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return jsonStr
    }

}