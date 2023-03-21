package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun MyScaffold(
    title: String = "",
    backgroundColor: Color = AppTheme.colors.background,
    onBack: (() -> Unit)? = null,
    topBar: @Composable () -> Unit = { MyTopBar(title, onBack = onBack) },
    bottomBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        backgroundColor = backgroundColor,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = fab,
        content = content
    )
}