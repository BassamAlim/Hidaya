package bassamalim.hidaya.core.ui.components

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.QViewType
import bassamalim.hidaya.features.quranViewer.QuranViewerState
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun QuranSettingsDialog(
    startState: QuranViewerState,
    pref: SharedPreferences,
    reciterNames: Array<String>,
    onDone: (QViewType) -> Unit
) {
    var viewType by remember { mutableStateOf(startState.viewType.ordinal) }
    val reciterIds = Array(reciterNames.size) { it.toString() }

    MyDialog(startState.settingsDialogShown) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryTitle(R.string.page_preferences)

            MyText(stringResource(R.string.display_method), Modifier.padding(horizontal = 16.dp))

            HorizontalRadioGroup(
                options = listOf(
                    stringResource(R.string.page),
                    stringResource(R.string.list_view)
                ),
                selection = viewType,
                onSelect = { selection ->
                    viewType = selection

                    pref.edit()
                        .putString("quran_view_type", if (selection == 1) "list" else "page")
                        .apply()
                }
            )

            SliderPref(
                pref = pref,
                prefObj = bassamalim.hidaya.core.data.Prefs.QuranTextSize,
                titleResId = R.string.text_size_title,
                valueRange = 20F..50F
            )

            MyHorizontalDivider()
            CategoryTitle(titleResId = R.string.recitation_settings)

            ListPref(
                sp = pref,
                titleResId = R.string.reciter,
                pref = bassamalim.hidaya.core.data.Prefs.AyaReciter,
                iconResId = -1,
                entries = reciterNames,
                values = reciterIds,
                bgColor = AppTheme.colors.background
            )

            SliderPref(
                pref = pref,
                prefObj = bassamalim.hidaya.core.data.Prefs.AyaRepeat,
                titleResId = R.string.aya_repeat,
                valueRange = 1f..11f,
                infinite = true
            )

            SwitchPref(
                pref = pref,
                prefObj = bassamalim.hidaya.core.data.Prefs.StopOnSuraEnd,
                titleResId = R.string.stop_on_sura_end,
                bgColor = AppTheme.colors.background,
                summary = ""
            )

            SwitchPref(
                pref = pref,
                prefObj = bassamalim.hidaya.core.data.Prefs.StopOnPageEnd,
                titleResId = R.string.stop_on_page_end,
                bgColor = AppTheme.colors.background,
                summary = ""
            )

            MyButton(
                text = stringResource(R.string.close),
                Modifier.fillMaxWidth()
            ) {
                onDone(QViewType.values()[viewType])
            }
        }
    }
}