package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
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
        onValueChangeFinished = onValueChangeFinished
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProgressSlider(
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
        onValueChangeFinished = onValueChangeFinished,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = remember { MutableInteractionSource() },
                thumbSize = DpSize(width = 20.dp, height = 20.dp)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(10.dp),
                enabled = enabled,
                thumbTrackGapSize = 0.dp,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    )
}

@Composable
fun MyValuedSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    progressMin: Float = 0f,
    enabled: Boolean = true,
    valueFormatter: (String) -> String = { it },
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
) {
    var sliderText = valueFormatter((value - progressMin).toInt().toString()).let {
        if (progressMin != 0f && it.toInt() > 0) "+$it" else it
    }

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
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            enabled = enabled,
            onValueChange = { newValue ->
                val progress = newValue - progressMin
                sliderText = valueFormatter(floor(progress).toInt().toString())
                onValueChange(progress)
            },
            onValueChangeFinished = onValueChangeFinished
        )

        MyText(sliderText)
    }
}