package bassamalim.hidaya.core.ui.components

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.PrefUtils

@Composable
fun ListPref(
    sp: SharedPreferences,
    titleResId: Int,
    pref: Prefs,
    iconResId: Int = -1,
    entries: Array<String>,
    values: Array<String>,
    bgColor: Color = AppTheme.colors.surface,
    onSelection: () -> Unit = {}
) {
    var shown by remember { mutableStateOf(false) }
    val initialValue = PrefUtils.getString(sp, pref)
    var selectedValue by remember { mutableStateOf(initialValue) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { shown = true }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 6.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconResId != -1) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = stringResource(titleResId),
                    Modifier.padding(end = 20.dp),
                    tint = AppTheme.colors.text
                )
            }

            Column {
                PreferenceTitle(titleResId)

                SummaryText(entries[values.indexOf(selectedValue)])
            }
        }

        if (shown) {
            val onSelect = { index: Int ->
                selectedValue = values[index]

                sp.edit()
                    .putString(pref.key, values[index])
                    .apply()

                onSelection()
            }

            Dialog(
                onDismissRequest = { shown = false }
            ) {
                Surface(
                    color = Color.Transparent
                ) {
                    Box(
                        Modifier
                            .background(
                                shape = RoundedCornerShape(16.dp),
                                color = AppTheme.colors.background
                            )
                    ) {
                        Column(
                            Modifier.padding(vertical = 20.dp, horizontal = 10.dp)
                        ) {
                            MyText(
                                text = stringResource(titleResId),
                                Modifier.padding(start = 10.dp, bottom = 10.dp)
                            )

                            Column(
                                Modifier
                                    .heightIn(1.dp, 400.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                entries.forEachIndexed { index, text ->
                                    Row(
                                        Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .fillMaxWidth()
                                            .padding(6.dp)
                                            .clickable { onSelect(index) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = index == values.indexOf(selectedValue),
                                            onClick = { onSelect(index) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = AppTheme.colors.accent,
                                                unselectedColor = AppTheme.colors.text
                                            )
                                        )

                                        MyText(text = text)
                                    }
                                }
                            }

                            MySquareButton(
                                stringResource(R.string.cancel),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = AppTheme.colors.background
                                ),
                                elevation = 0,
                                innerPadding = PaddingValues(0.dp)
                            ) {
                                shown = !shown
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchPref(
    sp: SharedPreferences,
    pref: Prefs,
    titleResId: Int,
    summary: String,
    bgColor: Color = AppTheme.colors.surface,
    onSwitch: (Boolean) -> Unit = {}
) {
    val initialValue = PrefUtils.getBoolean(sp, pref)
    var checked by remember { mutableStateOf(initialValue) }

    val onCheckChange = {
        checked = !checked

        sp.edit()
            .putBoolean(pref.key, checked)
            .apply()

        onSwitch(checked)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(bgColor)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckChange() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PreferenceTitle(titleResId, Modifier.padding(end = 40.dp))

                Switch(
                    checked = checked,
                    onCheckedChange = { onCheckChange() },
                    modifier = Modifier.height(10.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppTheme.colors.accent,
                        checkedTrackColor = AppTheme.colors.altAccent
                    )
                )
            }

            SummaryText(summary)
        }
    }
}

@Composable
fun SliderPref(
    sp: SharedPreferences,
    pref: Prefs,
    titleResId: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    infinite: Boolean = false,
    sliderFraction: Float = 0.8F,
    onValueChange: () -> Unit = {}
) {
    var value by remember { mutableStateOf(PrefUtils.getFloat(sp, pref)) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
    ) {
        PreferenceTitle(titleResId)

        MyValuedSlider(
            initialValue = value,
            valueRange = valueRange,
            infinite = infinite,
            sliderFraction = sliderFraction,
            onValueChange = { newValue -> value = newValue },
            onValueChangeFinished = {
                sp.edit()
                    .putFloat(pref.key, value)
                    .apply()

                onValueChange()
            }
        )
    }
}

@Composable
fun CategoryTitle(titleResId: Int) {
    MyText(
        stringResource(titleResId),
        Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        textColor = AppTheme.colors.accent)
}

@Composable
private fun PreferenceTitle(
    titleResId: Int, modifier: Modifier = Modifier
) {
    MyText(stringResource(titleResId), modifier)
}

@Composable
private fun SummaryText(text: String) {
    MyText(text = text, fontSize = 16.sp)
}