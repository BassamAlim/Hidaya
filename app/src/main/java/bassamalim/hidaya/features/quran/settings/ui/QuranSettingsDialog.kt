package bassamalim.hidaya.features.quran.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.core.ui.components.HorizontalRadioGroup
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import bassamalim.hidaya.features.settings.ui.CategoryTitle
import bassamalim.hidaya.features.settings.ui.MenuSetting
import bassamalim.hidaya.features.settings.ui.SliderPref
import bassamalim.hidaya.features.settings.ui.SwitchSetting

@Composable
fun QuranSettingsDialog(
    viewModel: QuranSettingsViewModel,
    shown: Boolean,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyDialog(
        shown = shown
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryTitle(titleResId = R.string.page_preferences)

            MyText(
                text = stringResource(R.string.display_method),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalRadioGroup(
                selection = state.viewType,
                items = QuranViewType.entries,
                entries = stringArrayResource(R.array.quran_view_type_entries),
                onSelect = viewModel::onViewTypeChange
            )

            SliderPref(
                value = state.textSize,
                title = stringResource(R.string.text_size_title),
                valueRange = 20F..50F,
                numeralsLanguage = viewModel.numeralsLanguage,
                onValueChange = viewModel::onTextSizeChange
            )

            MyHorizontalDivider()
            CategoryTitle(titleResId = R.string.recitation_settings)

            MenuSetting(
                selection = state.reciterId,
                items = viewModel.reciterIds,
                entries = viewModel.reciterNames.toTypedArray(),
                title = stringResource(R.string.reciter),
                iconResId = -1,
                bgColor = AppTheme.colors.background,
                onSelection = viewModel::onReciterChange
            )

            HorizontalRadioGroup(
                selection = state.repeatMode,
                items = VerseRepeatMode.entries,
                entries = stringArrayResource(R.array.quran_repeat_mode_entries),
                onSelect = viewModel::onRepeatModeChange
            )

            SwitchSetting(
                value = state.shouldStopOnSuraEnd,
                title = stringResource(R.string.stop_on_sura_end),
                bgColor = AppTheme.colors.background,
                summary = "",
                onSwitch = viewModel::onShouldStopOnSuraEndChange
            )

            SwitchSetting(
                value = state.shouldStopOnPageEnd,
                title = stringResource(R.string.stop_on_page_end),
                bgColor = AppTheme.colors.background,
                summary = "",
                onSwitch = viewModel::onShouldStopOnPageEndChange
            )

            BottomBar(
                onDone = onDone,
                onCancel = viewModel::onCancel,
                onSave = viewModel::onSave
            )
        }
    }
}

@Composable
private fun BottomBar(
    onDone: () -> Unit,
    onCancel: (() -> Unit) -> Unit,
    onSave: (() -> Unit) -> Unit
) {
    MyRow {
        MySquareButton(
            text = stringResource(R.string.cancel),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onCancel(onDone) }
        )

        MySquareButton(
            text = stringResource(R.string.save),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSave(onDone) }
        )
    }
}