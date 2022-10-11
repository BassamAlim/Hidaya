package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun MyScaffold(
    title: String,
    onBackPressed: () -> Unit,
    topBar: @Composable () -> Unit = {
        MyTopBar(
            title,
            onBackPressed = onBackPressed
        )
    },
    bottomBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        backgroundColor = AppTheme.colors.background,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = fab,
        content = content
    )
}