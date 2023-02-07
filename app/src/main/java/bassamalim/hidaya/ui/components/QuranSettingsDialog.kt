package bassamalim.hidaya.ui.components

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.state.QuranViewerState
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun QuranSettingsDialog(
    startState: QuranViewerState,
    pref: SharedPreferences,
    reciterNames: Array<String>,
    onDone: () -> Unit
) {
    val viewType = startState.viewType.ordinal
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
                    pref.edit()
                        .putString("quran_view_type", if (selection == 1) "list" else "page")
                        .apply()
                }
            )

            SliderPref(
                pref = pref,
                prefObj = Prefs.QuranTextSize,
                titleResId = R.string.text_size_title,
                valueRange = 20F..50F
            )

            MyHorizontalDivider()
            CategoryTitle(titleResId = R.string.recitation_settings)

            ListPref(
                sp = pref,
                titleResId = R.string.reciter,
                pref = Prefs.AyaReciter,
                iconResId = -1,
                entries = reciterNames,
                values = reciterIds,
                bgColor = AppTheme.colors.background
            )

            SliderPref(
                pref = pref,
                prefObj = Prefs.AyaRepeat,
                titleResId = R.string.aya_repeat,
                valueRange = 1F..11F,
                infinite = true
            )

            SwitchPref(
                pref = pref,
                prefObj = Prefs.StopOnSuraEnd,
                titleResId = R.string.stop_on_sura_end,
                bgColor = AppTheme.colors.background,
                summary = ""
            )

            SwitchPref(
                pref = pref,
                prefObj = Prefs.StopOnPageEnd,
                titleResId = R.string.stop_on_page_end,
                bgColor = AppTheme.colors.background,
                summary = ""
            )

            MyButton(
                text = stringResource(R.string.close),
                Modifier.fillMaxWidth()
            ) {
                onDone()
            }
        }
    }
}