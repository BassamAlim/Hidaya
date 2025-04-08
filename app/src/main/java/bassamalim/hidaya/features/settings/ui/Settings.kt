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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.MyValuedSlider

@Composable
fun <V> MenuSetting(
    selection: V,
    items: Array<V>,
    entries: Array<String>,
    title: String,
    icon: Any? = null,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                if (icon is ImageVector) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                else if (icon is Painter) {
                    Icon(
                        painter = icon,
                        contentDescription = title,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                else if (icon is Int) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = title,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(end = 20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
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
                            color = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            Modifier.padding(vertical = 20.dp, horizontal = 10.dp)
                        ) {
                            MyText(
                                text = title,
                                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp),
                                textColor = MaterialTheme.colorScheme.onSurface
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
                                                selectedColor = MaterialTheme.colorScheme.primary,
                                                unselectedColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        MyText(
                                            text = text,
                                            textColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            MyRectangleButton(
                                text = stringResource(R.string.cancel),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                elevation = 0,
                                innerPadding = PaddingValues(0.dp),
                                onClick = { isShown = !isShown }
                            )
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
    summary: String? = null,
    padding: PaddingValues = PaddingValues(vertical = 6.dp, horizontal = 16.dp),
    enabled: Boolean = true,
    onSwitch: (Boolean) -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSwitch(!value) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                PreferenceTitle(title, Modifier.padding(end = 40.dp))

                if (summary != null) {
                    SummaryText(summary)
                }
            }

            Switch(
                checked = value,
                onCheckedChange = { onSwitch(!value) },
                modifier = Modifier.height(10.dp),
                enabled = enabled
            )
        }
    }
}

@Composable
fun SliderPref(
    value: Float,
    title: String,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (String) -> String,
    enabled: Boolean = true,
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
            valueFormatter = valueFormatter,
            enabled = enabled,
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
            .padding(top = 15.dp, bottom = 8.dp, start = 15.dp, end = 15.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        textColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun PreferenceTitle(title: String, modifier: Modifier = Modifier) {
    MyText(
        text = title,
        modifier = modifier,
        textColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SummaryText(text: String) {
    MyText(
        text = text,
        fontSize = 16.sp,
        textColor = MaterialTheme.colorScheme.onSurface
    )
}