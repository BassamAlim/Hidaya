package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MyColumn(
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        content()
    }
}

@Composable
fun MyRow(
    modifier: Modifier = Modifier,
    content: RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
    ) {
        content()
    }
}