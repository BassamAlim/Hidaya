package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.dbs.AyatDB
import bassamalim.hidaya.models.QuranSearcherMatch
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import java.util.regex.Pattern

class QuranSearcherActivity : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var allAyat: List<AyatDB?>
    private val matches = mutableStateListOf<QuranSearcherMatch>()
    private lateinit var names: List<String>
    private val maxMatchesIndex = mutableStateOf(0)
    private lateinit var maxMatchesItems: Array<String>
    private lateinit var language: String
    private var searched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        init()

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun init() {
        val db = DBUtils.getDB(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        allAyat = db.ayahDao().getAll()

        names =
            if (language == "en") db.suarDao().getNamesEn()
            else db.suarDao().getNames()

        initMaxMatches()
    }

    private fun initMaxMatches() {
        maxMatchesIndex.value = pref.getInt("quran_searcher_max_matches_index", 0)

        maxMatchesItems =
            if (language == "en") resources.getStringArray(R.array.searcher_matches_en)
            else resources.getStringArray(R.array.searcher_matches)
    }

    private fun search(text: String, highlightColor: Color) {
        matches.clear()

        for (i in allAyat.indices) {
            val a = allAyat[i]!!
            val string = a.aya_text_emlaey

            val matcher = Pattern.compile(text).matcher(string)
            if (matcher.find()) {
                val annotatedString = buildAnnotatedString {
                    append(string)

                    do {
                        addStyle(
                            style = SpanStyle(color = highlightColor),
                            start = matcher.start(),
                            end = matcher.end()
                        )
                    } while (matcher.find())
                }

                matches.add(
                    QuranSearcherMatch(
                        a.sura_num, a.aya_num, names[a.sura_num], a.page,
                        annotatedString, a.aya_tafseer
                    )
                )

                searched = true
                if (matches.size == maxMatchesItems[maxMatchesIndex.value].toInt()) return
            }
        }
    }

    @Composable
    private fun UI() {
        val textState = remember { mutableStateOf(TextFieldValue("")) }
        val highlightColor = AppTheme.colors.accent

        MyScaffold(stringResource(id = R.string.quran_searcher)) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyText(
                        text = stringResource(id = R.string.search_for_quran_text)
                    )

                    SearchComponent(
                        state = textState,
                        hint = stringResource(id = R.string.search),
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
                        MyText(
                            text = stringResource(id = R.string.max_num_of_marches),
                            fontSize = 18.sp
                        )

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
                            text = stringResource(id = R.string.no_matches),
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
                                    Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        MyText(
                                            "${stringResource(R.string.sura)} ${item.suraName}"
                                        )

                                        MyText(
                                            "${stringResource(R.string.page)} " +
                                                    LangUtils.translateNums(
                                                        this@QuranSearcherActivity,
                                                        item.pageNum.toString(),
                                                        false
                                                    )
                                        )
                                    }

                                    MyText(
                                        text = "${stringResource(id = R.string.aya_number)} " +
                                            LangUtils.translateNums(
                                                this@QuranSearcherActivity,
                                                item.ayaNum.toString(),
                                                false
                                            ),
                                        modifier = Modifier.padding(6.dp)
                                    )
                                    MyText(
                                        text = item.text,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                    MyText(
                                        text = "${stringResource(R.string.tafseer)}: ${item.tafseer}",
                                        modifier = Modifier.padding(6.dp)
                                    )

                                    MyButton(
                                        text = stringResource(id = R.string.go_to_page),
                                        textColor = AppTheme.colors.accent,
                                        elevation = 0,
                                        innerPadding = PaddingValues(0.dp),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        val intent = Intent(
                                            this@QuranSearcherActivity,
                                            QuranViewer::class.java
                                        )
                                        intent.action = "by_page"
                                        intent.putExtra("page", item.pageNum)
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}