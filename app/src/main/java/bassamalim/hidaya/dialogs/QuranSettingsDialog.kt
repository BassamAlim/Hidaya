package bassamalim.hidaya.dialogs

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.ui.components.*

class QuranSettingsDialog(
    private val pref: SharedPreferences,
    db: AppDatabase,
    private val shown: MutableState<Boolean>,
    private val refresh: () -> Unit
) {

    private val viewType = mutableStateOf(
        if (pref.getString("quran_view_type", "page") == "list") 1
        else 0
    )
    private val reciterNames = db.ayatRecitersDao().getNames().toTypedArray()
    private val reciterIds = Array(reciterNames.size) { it.toString() }

    @Composable
    fun Dialog() {
        MyDialog(shown) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CategoryTitle(titleResId = R.string.page_preferences)

                MyText(stringResource(R.string.display_method), Modifier.padding(horizontal = 16.dp))

                HorizontalRadioGroup(
                    options = listOf(
                        stringResource(R.string.page),
                        stringResource(R.string.list_view)
                    ),
                    selection = viewType,
                    onSelect = { selection ->
                        pref.edit()
                            .putString("quran_view_type", if (selection == 1) "list" else "page")
                            .apply()
                    }
                )

                SliderPref(
                    pref = pref,
                    keyResId = R.string.quran_text_size_key,
                    titleResId = R.string.text_size_title,
                    defaultValue = 30,
                    valueRange = 20F..50F
                )

                MyHorizontalDivider()
                CategoryTitle(titleResId = R.string.recitation_settings)

                ListPref(
                    pref = pref,
                    titleResId = R.string.reciter,
                    keyResId = R.string.aya_reciter_key,
                    iconResId = -1,
                    entries = reciterNames,
                    values = reciterIds,
                    defaultValue = "13"
                )

                SliderPref(
                    pref = pref,
                    keyResId = R.string.aya_repeat_key,
                    titleResId = R.string.aya_repeat,
                    defaultValue = 1,
                    valueRange = 1F..11F,
                    infinite = true
                )

                SwitchPref(
                    pref = pref,
                    keyResId = R.string.stop_on_sura_key,
                    titleResId = R.string.stop_on_sura_end
                )

                SwitchPref(
                    pref = pref,
                    keyResId = R.string.stop_on_page_key,
                    titleResId = R.string.stop_on_page_end
                )

                MyButton(
                    text = stringResource(R.string.close),
                    Modifier.fillMaxWidth()
                ) {
                    shown.value = false
                    refresh()
                }
            }
        }
    }

}