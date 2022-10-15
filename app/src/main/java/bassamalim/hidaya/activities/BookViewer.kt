package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.gson.Gson

class BookViewer : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private val gson = Gson()
    private var bookId = 0
    private var chapterId = 0
    private lateinit var doors: Array<Book.BookChapter.BookDoor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        val intent = intent
        bookId = intent.getIntExtra("book_id", 0)
        chapterId = intent.getIntExtra("chapter_id", 0)
        val bookTitle = intent.getStringExtra("book_title")!!

        doors = getDoors()

        setContent {
            AppTheme {
                UI(title = bookTitle)
            }
        }
    }

    private fun getDoors(): Array<Book.BookChapter.BookDoor> {
        val path = getExternalFilesDir(null).toString() + "/Books/" + bookId + ".json"
        val jsonStr = FileUtils.getJsonFromDownloads(path)
        val book = gson.fromJson(jsonStr, Book::class.java)
        return book.chapters[chapterId].doors
    }

    @Composable
    private fun UI(title: String) {
        val textSize = remember { mutableStateOf(pref.getInt(getString(R.string.alathkar_text_size_key), 15)) }

        MyScaffold(
            title = title,
            bottomBar = {
                MyReadingBottomBar(
                    textSizeState = textSize
                ) {
                    textSize.value = it.toInt()

                    pref.edit()
                        .putInt(getString(R.string.alathkar_text_size_key), it.toInt())
                        .apply()
                }
            }
        ) {
            MyLazyColumn(
                modifier = Modifier.padding(it),
                lazyList = {
                    items(items = doors) { item ->
                        DoorCard(door = item, textSize)
                    }
                }
            )
        }
    }

    @Composable
    private fun DoorCard(door: Book.BookChapter.BookDoor, textSize: MutableState<Int>) {
        val textSizeMargin = 15
        MySurface(
            Modifier.padding(vertical = 3.dp, horizontal = 5.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    text = door.doorTitle,
                    modifier = Modifier.padding(10.dp),
                    fontSize = (textSize.value + textSizeMargin).sp,
                    fontWeight = FontWeight.Bold
                )

                MyText(
                    text = door.text,
                    modifier = Modifier.padding(10.dp),
                    fontSize = (textSize.value + textSizeMargin).sp,
                    textColor = AppTheme.colors.strongText
                )
            }
        }
    }

}