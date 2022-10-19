package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun MyScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    topBar: @Composable () -> Unit = { MyTopBar(title, onBack = onBack) },
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