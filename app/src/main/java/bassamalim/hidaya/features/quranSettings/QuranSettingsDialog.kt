package bassamalim.hidaya.features.quranSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.ui.components.CategoryTitle
import bassamalim.hidaya.core.ui.components.HorizontalRadioGroup
import bassamalim.hidaya.core.ui.components.ListPref
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SliderPref
import bassamalim.hidaya.core.ui.components.SwitchPref
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun QuranSettingsDlg(
    vm: QuranSettingsVM,
    shown: Boolean,
    mainOnDone: () -> Unit
) {
    MyDialog(
        shown = shown
    ) {
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
                initialSelection = vm.viewType.ordinal,
                onSelect = { selection ->
                    vm.onViewTypeCh(selection)
                }
            )

            SliderPref(
                sp = vm.sp,
                pref = Prefs.QuranTextSize,
                titleResId = R.string.text_size_title,
                valueRange = 20F..50F
            )

            MyHorizontalDivider()
            CategoryTitle(titleResId = R.string.recitation_settings)

            ListPref(
                sp = vm.sp,
                titleResId = R.string.reciter,
                pref = Prefs.AyaReciter,
                iconResId = -1,
                entries = vm.reciterNames.toTypedArray(),
                values = vm.reciterIds,
                bgColor = AppTheme.colors.background
            )

            SliderPref(
                sp = vm.sp,
                pref = Prefs.AyaRepeat,
                titleResId = R.string.aya_repeat,
                valueRange = 1f..11f,
                infinite = true
            )

            SwitchPref(
                sp = vm.sp,
                pref = Prefs.StopOnSuraEnd,
                titleResId = R.string.stop_on_sura_end,
                bgColor = AppTheme.colors.background,
                summary = ""
            )

            SwitchPref(
                sp = vm.sp,
                pref = Prefs.StopOnPageEnd,
                titleResId = R.string.stop_on_page_end,
                bgColor = AppTheme.colors.background,
                summary = ""
            )

            MySquareButton(
                text = stringResource(R.string.close),
                Modifier.fillMaxWidth()
            ) {
                vm.onDone(mainOnDone)
            }
        }
    }
}