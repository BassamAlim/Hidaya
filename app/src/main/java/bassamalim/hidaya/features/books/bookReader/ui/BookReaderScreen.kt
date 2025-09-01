package bassamalim.hidaya.features.books.bookReader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.core.models.BookContent
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.ReaderBottomBar

@Composable
fun BookReaderScreen(viewModel: BookReaderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(
        title = state.bookTitle,
        bottomBar = {
            ReaderBottomBar(textSize = state.textSize, onSeek = viewModel::onTextSizeChange)
        }
    ) { padding ->
        MyLazyColumn(
            modifier = Modifier.padding(padding),
            lazyList = {
                items(state.doors) { item ->
                    DoorCard(door = item, textSize = state.textSize)
                }
            }
        )
    }
}

@Composable
private fun DoorCard(door: BookContent.Chapter.Door, textSize: Float) {
    val textSizeMargin = 15
    Column(
        modifier = Modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyText(
            text = door.title,
            fontSize = (textSize + textSizeMargin).sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        MyText(text = door.text, fontSize = (textSize + textSizeMargin).sp)
    }

    HorizontalDivider(Modifier.padding(vertical = 10.dp))
}