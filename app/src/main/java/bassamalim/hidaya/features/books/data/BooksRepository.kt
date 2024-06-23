package bassamalim.hidaya.features.books.data

import android.app.Application
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.BooksPreferencesRepository
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.utils.FileUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val app: Application,
    private val db: AppDatabase,
    private val appSettingsPrefsRepository: AppSettingsPreferencesRepository,
    private val booksPreferencesRepository: BooksPreferencesRepository,
    private val gson: Gson
) {

    private val prefix = "/Books/"

    suspend fun getLanguage() = appSettingsPrefsRepository.flow.first()
        .language

    fun getBooks() = db.booksDao().getAll()

    suspend fun getShowTutorial() =
        booksPreferencesRepository.flow.first()
            .shouldShowTutorial

    suspend fun setDoNotShowAgain() {
        booksPreferencesRepository.update { it.copy(
            shouldShowTutorial = false
        )}
    }

    fun download(bookId: Int): FileDownloadTask {
        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference with an initial file path and name
        val fileRef = storageRef.child("${prefix.substring(1)}$bookId.json")

        FileUtils.createDir(app, prefix)
        val file = File("${app.getExternalFilesDir(null)}/$prefix/${fileRef.name}")
        file.createNewFile()

        return fileRef.getFile(file)
    }

    fun isDownloaded(bookId: Int): Boolean {
        val dir = File(app.getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return false

        val files = dir.listFiles()
        for (i in 0 until files!!.size) {
            val name = files[i].name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                if (num == bookId) return true
            } catch (ignored: NumberFormatException) {}
        }

        return false
    }

    fun isDownloading(bookId: Int): Boolean {
        val path = app.getExternalFilesDir(null).toString() + prefix + bookId + ".json"

        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return try {
            gson.fromJson(jsonStr, Book::class.java)
            false
        } catch (e: Exception) {
            true
        }
    }

    fun deleteBook(bookId: Int) {
        FileUtils.deleteFile(
            context = app,
            path = "${prefix}$bookId.json"
        )
    }

}