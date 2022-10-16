package bassamalim.hidaya.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.database.dbs.SuarDB
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Sura
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson

class QuranScreen(
    private val context: Context,
    private val pref: SharedPreferences
): NavigationScreen() {

    private val db = DBUtils.getDB(context)
    private val gson = Gson()
    private val types = hashMapOf(0 to ListType.All, 1 to ListType.Favorite)
    private lateinit var names: List<String>
    private val bookmarkedPage = mutableStateOf(pref.getInt("bookmarked_page", -1))

    override fun onResume() {
        bookmarkedPage.value = pref.getInt("bookmarked_page", -1)
    }

    private fun getItems(type: ListType): List<Sura> {
        val items = ArrayList<Sura>()
        val suras = getSuras()

        val surat = context.getString(R.string.sura)
        for (i in suras.indices) {
            val sura = suras[i]

            if (type == ListType.All || (type == ListType.Favorite && sura.favorite == 1))
                items.add(
                    Sura(
                        sura.sura_id, "$surat ${names[sura.sura_id]}",
                        sura.search_name!!, sura.tanzeel, mutableStateOf(sura.favorite)
                    )
                )
        }
        return items
    }

    private fun getSuras(): List<SuarDB> {
        names = if (PrefUtils.getLanguage(context) == "en") db.suarDao().getNamesEn() else db.suarDao().getNames()

        return db.suarDao().getAll()
    }

    private fun updateFavorites() {
        val favStr = gson.toJson(db.suarDao().getFav().toIntArray())
        val editor = pref.edit()
        editor.putString("favorite_suras", favStr)
        editor.apply()
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun QuranUI() {
        Column(
            Modifier.fillMaxSize()
        ) {
            val pagerState = rememberPagerState(pageCount = 2)
            val textState = remember { mutableStateOf(TextFieldValue("")) }

            val bookmarkedSura = pref.getInt("bookmarked_sura", -1)
            MyButton(
                text =
                    if (bookmarkedPage.value == -1) stringResource(id = R.string.no_bookmarked_page)
                    else {
                        "${context.getString(R.string.bookmarked_page)} " +
                                "${context.getString(R.string.page)} " +
                                "${LangUtils.translateNums(
                                    context, bookmarkedPage.value.toString()
                                )}, " +
                                "${context.getString(R.string.sura)} " +
                                if (PrefUtils.getLanguage(context, pref) == "en")
                                    db.suarDao().getNameEn(bookmarkedSura)
                                else db.suarDao().getName(bookmarkedSura)
                    },
                fontSize = 18.sp,
                textColor = AppTheme.colors.accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                innerPadding = PaddingValues(vertical = 4.dp)
            ) {
                if (bookmarkedPage.value != -1) {
                    val intent = Intent(context, QuranViewer::class.java)
                    intent.action = "by_page"
                    intent.putExtra("page", bookmarkedPage.value)
                    context.startActivity(intent)
                }
            }

            TabLayout(
                pagerState = pagerState,
                pagesInfo = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorite)
                ),
                extraComponents = {
                    SearchView(
                        state = textState,
                        hint = stringResource(id = R.string.quran_query_hint),
                        onSubmit = {
                            try {
                                val num = textState.value.text.toInt()
                                if (num in 1..604) {
                                    val openPage = Intent(context, QuranViewer::class.java)
                                    openPage.action = "by_page"
                                    openPage.putExtra("page", num)
                                    context.startActivity(openPage)
                                }
                                else
                                    Toast.makeText(
                                        context, context.getString(R.string.page_does_not_exist), Toast.LENGTH_SHORT
                                    ).show()
                            } catch (_: NumberFormatException) {}
                        }
                    )
                }
            ) { page ->
                Tab(items = getItems(types[page]!!), textState)
            }
        }
    }

    @Composable
    private fun Tab(
        items: List<Sura>,
        textState: MutableState<TextFieldValue>
    ) {
        val context = LocalContext.current

        MyLazyColumn(
            lazyList = {
                items(
                    items = items.filter { item ->
                        item.searchName.contains(textState.value.text, ignoreCase = true)
                    }
                ) { item ->
                    MySurface(
                        onClick = {
                            val intent = Intent(context, QuranViewer::class.java)
                            intent.action = "by_surah"
                            intent.putExtra("surah_id", item.id)
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                top = 8.dp, bottom = 8.dp, start = 14.dp, end = 8.dp
                            ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id =
                                    if (item.tanzeel == 0) R.drawable.ic_kaaba
                                    else R.drawable.ic_madina
                                ),
                                contentDescription = stringResource(
                                    id = R.string.tanzeel_view_description
                                )
                            )

                            MyText(
                                text = item.suraName,
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(10.dp)
                            )

                            MyFavBtn(
                                fav = item.favorite.value
                            ) {
                                if (item.favorite.value == 0) item.favorite.value = 1
                                else item.favorite.value = 0
                                updateFavorites()
                            }
                        }
                    }
                }
            }
        )
    }
}