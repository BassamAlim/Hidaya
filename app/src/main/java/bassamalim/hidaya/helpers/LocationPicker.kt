package bassamalim.hidaya.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.CityDB
import bassamalim.hidaya.database.dbs.CountryDB
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.utils.PrefUtils

class LocationPicker(
    private val context: Context,
    pref: SharedPreferences,
    private val db: AppDatabase,
    private val shown: MutableState<Boolean>,
    private val onSelection: (Int, Int) -> Unit
) {

    private val language = PrefUtils.getLanguage(context, pref)
    private var mode = mutableStateOf(0)  // 0 : country , 1 : city
    private var countryId = 0

    private fun getCountryItems(): List<CountryDB> {
        return db.countryDao().getAll().sortedBy { countryDB: CountryDB ->
            if (language == "en") countryDB.name_en
            else countryDB.name_ar
        }
    }

    private fun getCityItems(): List<CityDB> {
        return if (language == "en") db.cityDao().getTopEn(countryId, "").toList()
        else db.cityDao().getTopAr(countryId, "").toList()
    }

    @Composable
    fun UI() {
        MyScaffold(
            topBar = {
                MyTopBar(
                    title = context.getString(
                        if (mode.value == 0) R.string.choose_country else R.string.choose_city
                    ),
                    onBack = {
                        if (mode.value == 1) mode.value = 0
                        else shown.value = false
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val searchText = remember { mutableStateOf(TextFieldValue("")) }
                SearchComponent(
                    value = searchText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )

                MyLazyColumn(
                    lazyList = {
                        if (mode.value == 0) {
                            items(
                                items =  getCountryItems().filter { item ->
                                    item.name_en.contains(searchText.value.text, ignoreCase = true)
                                            || item.name_ar.contains(searchText.value.text)
                                }
                            ) { item ->
                                MyButton(
                                    text = if (language == "en") item.name_en else item.name_ar,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    countryId = item.id
                                    mode.value = 1

                                    searchText.value = TextFieldValue("")
                                }
                            }
                        }
                        else {
                            items(
                                items = getCityItems().filter { item ->
                                    item.nameEn.contains(searchText.value.text, ignoreCase = true)
                                            || item.nameAr.contains(searchText.value.text)
                                }
                            ) { item ->
                                MyButton(
                                    text = if (language == "en") item.nameEn else item.nameAr,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    onSelection(item.countryId, item.id)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

}