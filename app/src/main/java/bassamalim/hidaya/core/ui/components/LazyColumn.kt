package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyLazyColumn(
    modifier: Modifier = Modifier,
    lazyList: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = rememberLazyListState()
    ) {
        lazyList()
    }
}