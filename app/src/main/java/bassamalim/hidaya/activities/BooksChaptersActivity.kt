package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookChapter
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson

class BooksChaptersActivity : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private val gson: Gson = Gson()
    private var favStates = mutableStateListOf<Int>()
    private lateinit var book: Book
    private var bookId = 0
    private var bookTitle = ""
    private val types = hashMapOf(0 to ListType.All, 1 to ListType.Favorite)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        val intent = intent
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

        val favsStr = pref.getString("book" + bookId + "_favs", "")!!
        if (favsStr.isEmpty()) initStates()
        else favStates =
            gson.fromJson(favsStr, SnapshotStateList::class.java) as SnapshotStateList<Int>
    }

    private fun initStates() {
        for (chapter in book.chapters) favStates.add(0)
    }

    private fun getItems(type: ListType): List<BookChapter> {
        val items = ArrayList<BookChapter>()
        for (i in book.chapters.indices) {
            val chapter = book.chapters[i]
            if (type == ListType.All || type == ListType.Favorite && favStates[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }
        return items
    }

    private fun updateFavorites() {
        val favStr = gson.toJson(favStates)
        pref.edit()
            .putString("book" + bookId + "_favs", favStr)
            .apply()
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun UI(title: String) {
        MyScaffold(title) {
            val pagerState = rememberPagerState(pageCount = 2)
            val textState = remember { mutableStateOf(TextFieldValue("")) }

            TabLayout(
                pagerState = pagerState,
                pagesInfo = listOf(
                    getString(R.string.all),
                    getString(R.string.favorite)
                ),
                extraComponents = {
                    SearchView(
                        state = textState,
                        hint = stringResource(id = R.string.search)
                    )
                }
            ) { page ->
                Tab(items = getItems(types[page]!!), textState)
            }
        }
    }

    @Composable
    private fun Tab(
        items: List<BookChapter>,
        textState: MutableState<TextFieldValue>
    ) {
        val context = LocalContext.current

        MyLazyColumn(
            lazyList = {
                items(
                    items = items.filter { item ->
                        item.title.contains(textState.value.text, ignoreCase = true)
                    }
                ) { item ->
                    MyBtnSurface(
                        text = item.title,
                        modifier = Modifier.padding(vertical = 3.dp, horizontal = 5.dp),
                        iconBtn = {
                            MyFavBtn(
                                fav = favStates[item.id]
                            ) {
                                if (favStates[item.id] == 1) favStates[item.id] = 0
                                else favStates[item.id] = 1
                                updateFavorites()
                            }
                        }
                    ) {
                        val intent = Intent(context, BookViewer::class.java)
                        intent.putExtra("book_id", bookId)
                        intent.putExtra("book_title", item.title)
                        intent.putExtra("chapter_id", item.id)
                        startActivity(intent)
                    }
                }
            }
        )
    }
}