package bassamalim.hidaya.ui.components

import android.app.Activity
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils

@Composable
fun CategoryTitle(titleResId: Int) {
    MyText(
        stringResource(id = titleResId),
        Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        textColor = AppTheme.colors.accent)
}

@Composable
fun ListPref(
    pref: SharedPreferences,
    titleResId: Int,
    keyResId: Int,
    iconResId: Int,
    entries: Array<String>,
    values: Array<String>,
    defaultValueResId: Int
) {
    val context = LocalContext.current
    val key = stringResource(id = keyResId)
    var shown by remember { mutableStateOf(false) }
    val initialValue = pref.getString(
        stringResource(id = keyResId),
        stringResource(id = defaultValueResId)
    )
    val selectedValue by remember { mutableStateOf(initialValue) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(AppTheme.colors.background)
            .clip(RoundedCornerShape(10.dp))
            .clickable { shown = true }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(id = titleResId),
                Modifier.padding(end = 20.dp)
            )

            Column {
                MyText(text = stringResource(id = titleResId))

                SummaryText(entries[values.indexOf(selectedValue)])
            }
        }

        if (shown) {
            val onSelect = { index: Int ->
                pref.edit()
                    .putString(key, values[index])
                    .apply()

                ActivityUtils.restartActivity(context as Activity)
            }

            Dialog({}) {
                Surface(
                    color = Color.Transparent
                ) {
                    Box(
                        Modifier.background(
                            shape = RoundedCornerShape(16.dp),
                            color = AppTheme.colors.background
                        )
                    ) {
                        Column(
                            Modifier.padding(vertical = 20.dp, horizontal = 30.dp)
                        ) {
                            MyText(
                                text = stringResource(id = titleResId),
                                Modifier.padding(bottom = 10.dp)
                            )

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

                            MyButton(
                                stringResource(id = bassamalim.hidaya.R.string.cancel),
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
    pref: SharedPreferences,
    keyResId: Int,
    titleResId: Int,
    defaultValue: Boolean = true,
    summary: MutableState<String>,
    onSwitch: (Boolean) -> Unit
) {
    val key = stringResource(keyResId)
    val initialValue = pref.getBoolean(key, defaultValue)
    var checked by remember { mutableStateOf(initialValue) }

    val onCheckChange = {
        checked = !checked

        pref.edit()
            .putBoolean(key, checked)
            .apply()

        onSwitch(checked)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(AppTheme.colors.background)
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
                MyText(text = stringResource(id = titleResId))

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

            SummaryText(text = summary.value)
        }
    }
}

@Composable
private fun SummaryText(text: String) {
    MyText(text = text, fontSize = 16.sp)
}