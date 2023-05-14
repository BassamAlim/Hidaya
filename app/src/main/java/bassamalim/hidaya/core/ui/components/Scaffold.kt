package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun MyScaffold(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppTheme.colors.background,
    onBack: (() -> Unit)? = null,
    topBar: @Composable () -> Unit = { MyTopBar(title, onBack = onBack) },
    bottomBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        backgroundColor = backgroundColor,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = fab,
        content = content
    )
}