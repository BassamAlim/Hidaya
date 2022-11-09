package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookChapter
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.gson.Gson

class BooksChaptersActivity : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private val gson: Gson = Gson()
    private var favs = mutableStateListOf<Int>()
    private lateinit var book: Book
    private var bookId = 0
    private var bookTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        bookId = intent.getIntExtra("book_id", 0)
        bookTitle = intent.getStringExtra("book_title")!!

        getData()

        setContent {
            AppTheme {
                UI(title = bookTitle)
            }
        }
    }

    private fun getData() {
        val path = getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        book = gson.fromJson(jsonStr, Book::class.java)

        setupFavs()
    }

    private fun setupFavs() {
        val favsStr = pref.getString("book" + bookId + "_favs", "")!!
        if (favsStr.isEmpty()) {
            for (chapter in book.chapters) favs.add(0)
        }
        else favs = gson.fromJson(
            favsStr, SnapshotStateList::class.java
        ) as SnapshotStateList<Int>
    }

    private fun getItems(type: ListType): List<BookChapter> {
        val items = ArrayList<BookChapter>()
        for (i in book.chapters.indices) {
            val chapter = book.chapters[i]
            if (type == ListType.All || type == ListType.Favorite && favs[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }
        return items
    }

    private fun updateFavorites() {
        val favStr = gson.toJson(favs)
        pref.edit()
            .putString("book" + bookId + "_favs", favStr)
            .apply()
    }

    @Composable
    private fun UI(title: String) {
        MyScaffold(title) {
            val textState = remember { mutableStateOf(TextFieldValue("")) }

            TabLayout(
                pageNames = listOf(
                    getString(R.string.all),
                    getString(R.string.favorite)
                ),
                searchComponent = {
                    SearchComponent(
                        state = textState,
                        hint = stringResource(R.string.search),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            ) { page ->
                Tab(items = getItems(ActivityUtils.getListType(page)), textState)
            }
        }
    }

    @Composable
    private fun Tab(
        items: List<BookChapter>,
        textState: MutableState<TextFieldValue>
    ) {
        MyLazyColumn(
            lazyList = {
                items(
                    items = items.filter { item ->
                        item.title.contains(textState.value.text, ignoreCase = true)
                    }
                ) { item ->
                    MyBtnSurface(
                        text = item.title,
                        iconBtn = {
                            MyFavBtn(
                                fav = favs[item.id]
                            ) {
                                if (favs[item.id] == 1) favs[item.id] = 0
                                else favs[item.id] = 1
                                updateFavorites()
                            }
                        }
                    ) {
                        startActivity(
                            Intent(this@BooksChaptersActivity, BookViewer::class.java)
                                .putExtra("book_id", bookId)
                                .putExtra("book_title", item.title)
                                .putExtra("chapter_id", item.id)
                        )
                    }
                }
            }
        )
    }
}