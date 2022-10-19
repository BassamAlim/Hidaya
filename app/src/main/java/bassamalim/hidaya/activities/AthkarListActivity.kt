package bassamalim.hidaya.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AthkarDB
import bassamalim.hidaya.models.AthkarItem
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.gson.Gson

class AthkarListActivity : ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private val gson = Gson()
    private var category = 0
    private lateinit var action: String
    private lateinit var db: AppDatabase
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        db = DBUtils.getDB(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        action = intent.action!!

        val title: String
        when (action) {
            "all" -> title = getString(R.string.all_athkar)
            "favorite" -> title = getString(R.string.favorite_athkar)
            else -> {
                category = intent.getIntExtra("category", 0)
                title =
                    if (language == "en") db.athkarCategoryDao().getNameEn(category)
                    else db.athkarCategoryDao().getName(category)
            }
        }

        setContent {
            AppTheme {
                UI(title)
            }
        }
    }

    private val data: List<AthkarDB>
        get() {
            return when (action) {
                "all" -> db.athkarDao().getAll()
                "favorite" -> db.athkarDao().getFavorites()
                else -> db.athkarDao().getList(category)
            }
        }

    private fun getItems(athkar: List<AthkarDB>): List<AthkarItem> {
        val items: MutableList<AthkarItem> = ArrayList()

        for (i in athkar.indices) {
            val thikr = athkar[i]

            if (language == "en" && !hasEn(thikr)) continue

            val name =
                if (language == "en") thikr.name_en!!
                else thikr.name!!

            items.add(
                AthkarItem(
                    thikr.id, thikr.category_id, name, mutableStateOf(thikr.favorite)
                )
            )
        }
        return items
    }

    private fun hasEn(thikr: AthkarDB): Boolean {
        val ts = db.thikrsDao().getThikrs(thikr.id)
        for (i in ts.indices) {
            val t = ts[i]
            if (t.getTextEn() != null && t.getTextEn()!!.length > 1) return true
        }
        return false
    }

    private fun updateFavorites() {
        val favAthkar = db.athkarDao().getFavs()
        val athkarJson = gson.toJson(favAthkar)
        pref.edit()
            .putString("favorite_athkar", athkarJson)
            .apply()
    }

    @Composable
    private fun UI(title: String) {
        MyScaffold(title) {
            val textState = remember { mutableStateOf(TextFieldValue("")) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
            ) {
                SearchView (
                    state = textState,
                    hint = stringResource(id = R.string.athkar_hint)
                )

                MyLazyColumn(
                    lazyList = {
                        items(
                            items = getItems(data).filter { item ->
                                item.name.contains(textState.value.text, ignoreCase = true)
                            }
                        ) { item ->
                            MyBtnSurface(
                                text = item.name,
                                iconBtn = {
                                    MyFavBtn(
                                        fav = item.favorite.value
                                    ) {
                                        if (item.favorite.value == 0) {
                                            db.athkarDao().setFav(item.id, 1)
                                            item.favorite.value = 1
                                        }
                                        else if (item.favorite.value == 1) {
                                            db.athkarDao().setFav(item.id, 0)
                                            item.favorite.value = 0
                                        }
                                        updateFavorites()
                                    }
                                }
                            ) {
                                val intent = Intent(
                                    this@AthkarListActivity, AthkarViewer::class.java
                                )
                                intent.action = action
                                intent.putExtra("thikr_id", item.id)
                                startActivity(intent)
                            }
                        }
                    }
                )
            }
        }
    }

}