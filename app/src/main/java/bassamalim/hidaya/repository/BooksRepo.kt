package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.data.database.AppDatabase
import bassamalim.hidaya.data.database.dbs.BooksDB
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.utils.FileUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.io.File
import javax.inject.Inject

class BooksRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    private val prefix = "/Books/"
    private val dir = File(context.getExternalFilesDir(null).toString() + prefix)
    val language = PrefUtils.getLanguage(pref)

    fun getBooks() = db.booksDao().getAll()

    fun getPath(itemId: Int) = "$prefix$itemId.json"

    fun setDoNotShowAgain() {
        pref.edit()
            .putBoolean(Prefs.ShowBooksTutorial.key, false)
            .apply()
    }

    fun download(item: BooksDB): FileDownloadTask {
        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference with an initial file path and name
        val fileRef = storageRef.child("Books/${item.id}.json")

        FileUtils.createDir(context, prefix)
        val file = File("${context.getExternalFilesDir(null)}/$prefix/${fileRef.name}")
        file.createNewFile()

        return fileRef.getFile(file)
    }

    fun isDownloaded(id: Int): Boolean {
        if (!dir.exists()) return false

        val files = dir.listFiles()
        for (i in 0 until files!!.size) {
            val name = files[i].name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                if (num == id) return true
            } catch (ignored: NumberFormatException) {}
        }

        return false
    }

    fun isDownloading(id: Int): Boolean {
        val path = context.getExternalFilesDir(null).toString() + "/Books/" + id + ".json"

        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return try {
            gson.fromJson(jsonStr, Book::class.java)
            false
        } catch (e: Exception) {
            true
        }
    }

}