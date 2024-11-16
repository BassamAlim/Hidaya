package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import kotlin.math.floor

@Composable
fun MySlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {}
) {
    Slider(
        value = value,
        valueRange = valueRange,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer,
            thumbColor = MaterialTheme.colorScheme.primary
        ),
        onValueChangeFinished = onValueChangeFinished
    )
}

@Composable
fun MyValuedSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    numeralsLanguage: Language,
    progressMin: Float = 0f,
    sliderFraction: Float = 0.8F,
    enabled: Boolean = true,
    infinite: Boolean = false,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
) {
    val context = LocalContext.current
    var sliderText = if (infinite && (value - progressMin) == valueRange.endInclusive)
        context.getString(R.string.infinite)
    else
        translateNums(
            numeralsLanguage = numeralsLanguage,
            string = (value - progressMin).toInt().toString()
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MySlider(
            value = value,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(fraction = sliderFraction),
            enabled = enabled,
            onValueChange = { newValue ->
                val progress = newValue - progressMin

                var progressStr = floor(progress).toInt().toString()
                if (progressMin != 0f && progress.toInt() > 0) progressStr += "+"
                sliderText =
                    if (infinite && progress == valueRange.endInclusive)
                        context.getString(R.string.infinite)
                    else translateNums(
                        numeralsLanguage = numeralsLanguage,
                        string = progressStr
                    )

                onValueChange(progress)
            },
            onValueChangeFinished = onValueChangeFinished
        )

        MyText(
            text = sliderText,
            textColor = MaterialTheme.colorScheme.primary
        )
    }
}