package bassamalim.hidaya.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyValuedSlider
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun <V> MenuSetting(
    selection: V,
    items: Array<V>,
    entries: Array<String>,
    title: String,
    iconResId: Int = -1,
    bgColor: Color = AppTheme.colors.surface,
    onSelection: (V) -> Unit = {}
) {
    var isShown by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { isShown = true }
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
                    contentDescription = title,
                    modifier = Modifier.padding(end = 20.dp),
                    tint = AppTheme.colors.text
                )
            }

            Column {
                PreferenceTitle(title)

                SummaryText(entries[items.indexOf(selection)])
            }
        }

        if (isShown) {
            Dialog(
                onDismissRequest = { isShown = false }
            ) {
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
                            Modifier.padding(vertical = 20.dp, horizontal = 10.dp)
                        ) {
                            MyText(
                                text = title,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
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
                                            .clickable {
                                                onSelection(items[index])
                                                isShown = false
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = index == items.indexOf(selection),
                                            onClick = {
                                                onSelection(items[index])
                                                isShown = false
                                            },
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
                                isShown = !isShown
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchSetting(
    value: Boolean,
    title: String,
    summary: String,
    bgColor: Color = AppTheme.colors.surface,
    onSwitch: (Boolean) -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(bgColor)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSwitch(!value) }
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
                PreferenceTitle(title, Modifier.padding(end = 40.dp))

                Switch(
                    checked = value,
                    onCheckedChange = { onSwitch(!value) },
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
    value: Float,
    title: String,
    valueRange: ClosedFloatingPointRange<Float>,
    numeralsLanguage: Language,
    infinite: Boolean = false,
    sliderFraction: Float = 0.8F,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
    ) {
        PreferenceTitle(title)

        MyValuedSlider(
            value = value,
            valueRange = valueRange,
            numeralsLanguage = numeralsLanguage,
            infinite = infinite,
            sliderFraction = sliderFraction,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@Composable
fun CategoryTitle(title: String) {
    MyText(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 15.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        textColor = AppTheme.colors.accent
    )
}

@Composable
fun PreferenceTitle(title: String, modifier: Modifier = Modifier) {
    MyText(text = title, modifier = modifier)
}

@Composable
private fun SummaryText(text: String) {
    MyText(
        text = text,
        fontSize = 16.sp
    )
}