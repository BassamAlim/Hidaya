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
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun myOnActivityCreated(activity: Activity) {
        onActivityCreateSetTheme(activity)
        onActivityCreateSetLocale(activity)
    }

    fun onActivityCreateSetTheme(activity: Activity): String {
        val theme: String? = PreferenceManager.getDefaultSharedPreferences(activity)
            .getString(
                activity.getString(R.string.theme_key),
                activity.getString(R.string.default_theme)
            )
        when (theme) {
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

    fun getDB(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
    }

    fun getLanguage(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): String {
        return pref.getString(
            context.getString(R.string.language_key), context.getString(R.string.default_language)
        )!!
    }

    fun getNumeralsLanguage(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): String {
        return pref.getString(
            context.getString(R.string.numerals_language_key),
            context.getString(R.string.default_language)
        )!!
    }

    fun getTimeFormat(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): PrayTimes.TF {
        val str = pref.getString(
            context.getString(R.string.time_format_key),
            context.getString(R.string.default_time_format)
        )!!

        return when(str) {
            "24h" -> PrayTimes.TF.H24
            else -> PrayTimes.TF.H12
        }
    }

    fun getUtcOffset(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        db: AppDatabase = getDB(context)
    ): Int {
        when (pref.getString("location_type", "auto")) {
            "auto" -> return TimeZone.getDefault().getOffset(Date().time) / 3600000
            "manual" -> {
                val cityId = pref.getInt("city_id", -1)

                if (cityId == -1) return 0

                val timeZoneId = db.cityDao().getCity(cityId).timeZone

                val timeZone = TimeZone.getTimeZone(timeZoneId)
                return timeZone.getOffset(Date().time) / 3600000
            }
            else -> return 0
        }
    }

    fun translateNumbers(context: Context, english: String, timeFormat: Boolean = false): String {
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

        if (getNumeralsLanguage(context) == "en") return eng

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

    fun formatTime(context: Context, gStr: String): String {
        var str = gStr

        val hour = "%d".format(str.split(':')[0].toInt())
        var minute = str.split(":")[1]
        minute = minute.replace("am", "")
        minute = minute.replace("pm", "")
        minute = minute.replace("ص", "")
        minute = minute.replace("م", "")
        minute = "%02d".format(minute.toInt())

        str = "$hour:$minute"

        val timeFormat = getTimeFormat(context)

        val h12Format = SimpleDateFormat("hh:mm aa", Locale.US)
        val h24Format = SimpleDateFormat("HH:mm", Locale.US)

        if (str[str.length-1].isDigit()) {  // Input is in 24h format
            return if (timeFormat == PrayTimes.TF.H24) str
            else {
                val date = h24Format.parse(str)
                val output = h12Format.format(date!!).lowercase()
                output
            }
        }
        else { // Input is in 12h format
            return if (timeFormat == PrayTimes.TF.H12) str
            else {
                val date = h12Format.parse(str)
                val output = h24Format.format(date!!)
                output
            }
        }
    }

    fun getTimes(
        context: Context, loc: Location, calendar: Calendar = Calendar.getInstance()
    ): Array<Calendar?> {
        val prayTimes = PrayTimes(context)
        val utcOffset = getUtcOffset(context).toDouble()

        return prayTimes.getPrayerTimes(loc.latitude, loc.longitude, utcOffset, calendar)
    }

    fun getStrTimes(
        context: Context, loc: Location, calendar: Calendar = Calendar.getInstance()
    ): ArrayList<String> {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val prayTimes = PrayTimes(context)
        val utcOffset = getUtcOffset(context, pref).toDouble()

        val timeFormat = getTimeFormat(context, pref)

        return prayTimes.getStrPrayerTimes(
            loc.latitude, loc.longitude, utcOffset, calendar, timeFormat
        )
    }

    fun reviveDb(context: Context) {
        val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        context.deleteDatabase("HidayaDB")

        val db = getDB(context)

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