package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.dialogs.FilterDialog
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.BookSearcherMatch
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.FileUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import java.io.File
import java.util.regex.Pattern

class BookSearcher : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private val gson = Gson()
    private lateinit var books: List<BooksDB>
    private lateinit var language: String
    private val selectedBooks = mutableStateListOf<Boolean>()
    private val filteredState = mutableStateOf(false)
    private val maxMatchesIndex = mutableStateOf(0)
    private lateinit var maxMatchesItems: Array<String>
    private val matches = mutableStateListOf<BookSearcherMatch>()
    private var searched = false
    private val filterDialogShown = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = ActivityUtils.myOnActivityCreated(this)[1]

        init()

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        db = DBUtils.getDB(this)

        books = db.booksDao().getAll()

        initSelectedBooks()
        initFilterIb()
        initMaxMatches()
    }

    private fun initSelectedBooks() {
        for (i in books.indices) selectedBooks.add(true)
        val json = PrefUtils.getString(pref, "selected_search_books", "")
        if (json.isNotEmpty()) {
            val boolArr =  gson.fromJson(json, BooleanArray::class.java)
            for (i in boolArr.indices) selectedBooks[i] = boolArr[i]
        }
    }

    private fun initFilterIb() {
        for (bool in selectedBooks) {
            if (!bool) {
                filteredState.value = true
                break
            }
        }
    }

    private fun initMaxMatches() {
        maxMatchesIndex.value = PrefUtils.getInt(
            pref, "books_searcher_max_matches_index", 0
        )

        maxMatchesItems =
            if (language == "en") resources.getStringArray(R.array.searcher_matches_en)
            else resources.getStringArray(R.array.searcher_matches)
    }

    private fun search(text: String, highlightColor: Color) {
        matches.clear()

        val prefix = "/Books/"
        val dir = File(getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return

        for (i in books.indices) {
            if (!selectedBooks[i] || !downloaded(i)) continue

            val jsonStr = FileUtils.getJsonFromDownloads(
                getExternalFilesDir(null).toString() + prefix + i + ".json"
            )
            val book =
                try {
                    gson.fromJson(jsonStr, Book::class.java)
                } catch (e: Exception) {
                    Log.e(Global.TAG, "Error in json read in BookSearcher")
                    e.printStackTrace()
                    continue
                }

            for (j in book.chapters.indices) {
                val chapter = book.chapters[j]

                for (k in chapter.doors.indices) {
                    val door = chapter.doors[k]
                    val doorText = door.text

                    val matcher = Pattern.compile(text).matcher(doorText)
                    if (matcher.find()) {
                        val annotatedString = buildAnnotatedString {
                            append(doorText)

                            do {
                                addStyle(
                                    style = SpanStyle(color = highlightColor),
                                    start = matcher.start(),
                                    end = matcher.end()
                                )
                            } while (matcher.find())
                        }

                        matches.add(
                            BookSearcherMatch(
                                i, book.bookInfo.bookTitle,
                                j, chapter.chapterTitle,
                                k, door.doorTitle, annotatedString
                            )
                        )

                        searched = true
                        if (matches.size == maxMatchesItems[maxMatchesIndex.value].toInt()) return
                    }
                }
            }
        }
    }

    private fun downloaded(id: Int): Boolean {
        val dir = File(getExternalFilesDir(null).toString() + "/Books/")

        if (!dir.exists()) return false

        val files = dir.listFiles()
        for (element in files!!) {
            val name = element.name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                if (num == id) return true
            } catch (ignored: NumberFormatException) {}
        }
        return false
    }

    @Composable
    private fun UI() {
        MyScaffold(stringResource(R.string.books_searcher)) { padding ->
            val highlightColor = AppTheme.colors.accent
            val textState = remember { mutableStateOf(TextFieldValue("")) }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyText(
                        text = stringResource(R.string.search_in_books),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    SearchComponent(
                        state = textState,
                        hint = stringResource(R.string.search),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        onSubmit = {
                            search(
                                text = textState.value.text,
                                highlightColor = highlightColor
                            )
                        }
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MyText(text = stringResource(R.string.selected_books))

                        MyIconBtn(
                            iconId = R.drawable.ic_filter,
                            description = stringResource(R.string.filter_search_description),
                            size = 30.dp,
                            tint =
                                if (filteredState.value) AppTheme.colors.secondary
                                else AppTheme.colors.weakText
                        ) {
                            filterDialogShown.value = true
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MyText(text = stringResource(R.string.max_num_of_marches))

                        MyDropDownMenu(
                            selectedIndex = maxMatchesIndex,
                            items = maxMatchesItems
                        ) { index ->
                            pref.edit()
                                .putInt("books_searcher_max_matches_index", index)
                                .apply()
                        }
                    }
                }

                if (matches.isEmpty()) {
                    if (searched)
                        MyText(
                            text = stringResource(R.string.books_no_matches),
                            modifier = Modifier.padding(top = 100.dp)
                        )
                }
                else {
                    MyLazyColumn(lazyList = {
                        items(
                            items = matches
                        ) { item ->
                            MySurface {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MyText(
                                        text = item.bookTitle,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                    MyText(
                                        text = item.chapterTitle,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                    MyText(
                                        text = item.doorTitle,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                    MyText(
                                        text = item.text,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    })
                }
            }

            if (filterDialogShown.value) {
                val bookTitles =
                    if (language == "en") db.booksDao().getTitlesEn()
                    else db.booksDao().getTitles()
                FilterDialog(
                    pref, gson, getString(R.string.choose_books),
                    bookTitles, selectedBooks, filteredState,
                    { if (searched) search(textState.value.text, highlightColor) },
                    "selected_search_books", filterDialogShown
                ).Dialog()
            }
        }
    }
}