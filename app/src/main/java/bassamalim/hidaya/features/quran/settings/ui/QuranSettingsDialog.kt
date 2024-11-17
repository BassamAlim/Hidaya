package bassamalim.hidaya.features.quran.settings.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.core.ui.components.FullScreenDialog
import bassamalim.hidaya.core.ui.components.HorizontalRadioGroup
import bassamalim.hidaya.core.ui.components.MyCheckbox
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import bassamalim.hidaya.features.settings.ui.CategoryTitle
import bassamalim.hidaya.features.settings.ui.MenuSetting
import bassamalim.hidaya.features.settings.ui.PreferenceTitle
import bassamalim.hidaya.features.settings.ui.SliderPref

@Composable
fun QuranSettingsDialog(viewModel: QuranSettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    FullScreenDialog(
        shown = true,
        onDismiss = viewModel::onDismiss
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryTitle(stringResource(R.string.page_preferences))

            MyText(
                text = stringResource(R.string.display_method),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalRadioGroup(
                selection = state.viewType,
                items = QuranViewType.entries,
                entries = stringArrayResource(R.array.quran_view_type_entries),
                onSelect = viewModel::onViewTypeChange,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyCheckbox(
                    isChecked = state.fillPage,
                    onCheckedChange = viewModel::onFillPageChange,
                    isEnabled = state.isFillPageEnabled
                )

                MyText(stringResource(R.string.fill_page))
            }

            SliderPref(
                value = state.textSize,
                title = stringResource(R.string.text_size_title),
                valueRange = 20F..50F,
                valueFormatter = viewModel::formatSliderValue,
                enabled = state.isTextSizeSliderEnabled,
                onValueChange = viewModel::onTextSizeChange,
            )

            MyHorizontalDivider()
            CategoryTitle(stringResource(R.string.recitation_settings))

            MenuSetting(
                selection = state.reciterId,
                items = viewModel.reciterIds,
                entries = viewModel.reciterNames.toTypedArray(),
                title = stringResource(R.string.reciter),
                iconResId = -1,
                onSelection = viewModel::onReciterChange
            )

            PreferenceTitle(
                title = stringResource(R.string.verse_repeat),
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp, start = 10.dp)
            )

            RepeatRadioGroup(
                selection = state.repeatMode,
                items = VerseRepeatMode.entries,
                entries = stringArrayResource(R.array.quran_repeat_mode_entries),
                onSelect = viewModel::onRepeatModeChange
            )

            SwitchSetting(
                title = stringResource(R.string.stop_on_sura_end),
                isOn = state.shouldStopOnSuraEnd,
                onSwitch = viewModel::onShouldStopOnSuraEndChange
            )

            SwitchSetting(
                title = stringResource(R.string.stop_on_sura_end),
                isOn = state.shouldStopOnPageEnd,
                onSwitch = viewModel::onShouldStopOnPageEndChange
            )

            BottomBar(onCancel = viewModel::onDismiss, onSave = viewModel::onSave)
        }
    }
}

@Composable
private fun RepeatRadioGroup(
    selection: VerseRepeatMode,
    items: List<VerseRepeatMode>,
    entries: Array<String>,
    onSelect: (VerseRepeatMode) -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1 until entries.lastIndex) {
                MyRectangleButton(
                    text = entries[i],
                    fontSize = 16.nsp,
                    textColor =
                        if (items[i] == selection) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    modifier =
                        if (items[i] == selection) Modifier
                            .weight(1F)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                        else Modifier.weight(1F),
                    padding = PaddingValues(horizontal = 5.dp),
                    onClick = { onSelect(items[i]) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // No repeat
            MyRectangleButton(
                text = entries[0],
                fontSize = 18.nsp,
                textColor =
                    if (items[0] == selection) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                modifier =
                if (items[0] == selection) Modifier
                    .weight(1F)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                else Modifier.weight(1F),
                padding = PaddingValues(horizontal = 5.dp),
                onClick = { onSelect(items[0]) }
            )

            // Infinite repeat
            MyRectangleButton(
                text = entries.last(),
                fontSize = 16.nsp,
                textColor =
                if (items.last() == selection) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                modifier =
                if (items.last() == selection) Modifier
                    .weight(1F)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                else Modifier.weight(1F),
                padding = PaddingValues(horizontal = 5.dp),
                onClick = { onSelect(items.last()) }
            )
        }


    }
}

@Composable
private fun SwitchSetting(title: String, isOn: Boolean, onSwitch: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PreferenceTitle(
            title = title,
            modifier = Modifier.padding(end = 40.dp)
        )

        Switch(checked = isOn, onCheckedChange = onSwitch)
    }
}

@Composable
private fun BottomBar(onCancel: () -> Unit, onSave: () -> Unit) {
    MyRow(
        Modifier.padding(top = 10.dp)
    ) {
        MyRectangleButton(
            text = stringResource(R.string.cancel),
            modifier = Modifier.weight(1f),
            onClick = onCancel
        )

        MyRectangleButton(
            text = stringResource(R.string.save),
            modifier = Modifier.weight(1f),
            onClick = onSave
        )
    }
}