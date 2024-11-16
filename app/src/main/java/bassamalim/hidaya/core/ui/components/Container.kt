package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyParentColumn(
    modifier: Modifier = Modifier,
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var adjustedModifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
    if (scroll) adjustedModifier = adjustedModifier.verticalScroll(rememberScrollState())

    Column(
        modifier = adjustedModifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
fun MyColumn(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    fillMaxWidth: Boolean = true,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    arrangement: Arrangement.Vertical = Arrangement.SpaceEvenly,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var modifier = modifier
    if (fillMaxWidth)
        modifier = modifier.fillMaxWidth(widthFraction)

    if (scrollable)
        modifier = modifier.verticalScroll(rememberScrollState())

    Column(
        verticalArrangement = arrangement,
        horizontalAlignment = alignment,
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun MyRow(
    modifier: Modifier = Modifier,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    arrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly,
    padding: PaddingValues = PaddingValues(horizontal = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = alignment,
        horizontalArrangement = arrangement,
        modifier = modifier
            .fillMaxWidth()
            .padding(padding)
    ) {
        content()
    }
}