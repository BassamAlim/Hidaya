package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.LangUtils.translateNums

@Composable
fun MySlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: () -> Unit = {},
    onValueChange: (Float) -> Unit
) {
    Slider(
        value = value,
        valueRange = valueRange,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        colors = SliderDefaults.colors(
            activeTrackColor = AppTheme.colors.accent,
            inactiveTrackColor = AppTheme.colors.altAccent,
            thumbColor = AppTheme.colors.accent
        ),
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun MyValuedSlider(
    initialValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    progressMin: Float = 0f,
    sliderFraction: Float = 0.8F,
    enabled: Boolean = true,
    infinite: Boolean = false,
    onValueChangeFinished: () -> Unit = {},
    onValueChange: (Float) -> Unit
) {
    val context = LocalContext.current
    var currentValue by remember { mutableStateOf(initialValue) }
    var sliderText by remember {
        mutableStateOf(
            translateNums(
                context, (initialValue - progressMin).toInt().toString()
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MySlider(
            value = currentValue,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(fraction = sliderFraction),
            enabled = enabled,
            onValueChange = { value ->
                currentValue = value

                val progress = currentValue - progressMin

                var progressStr = progress.toInt().toString()
                if (progressMin != 0f && progress > 0f) progressStr += "+"
                sliderText =
                    if (infinite && progress == valueRange.endInclusive)
                        context.getString(R.string.infinite)
                    else translateNums(context, progressStr)

                onValueChange(progress)
            },
            onValueChangeFinished = onValueChangeFinished
        )

        MyText(
            sliderText,
            textColor = AppTheme.colors.accent
        )
    }
}