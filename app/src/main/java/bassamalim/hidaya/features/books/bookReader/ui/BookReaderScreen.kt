package bassamalim.hidaya.features.books.bookReader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import bassamalim.hidaya.core.ui.components.MyReadingBottomBar
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun BookReaderScreen(viewModel: BookReaderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(
        title = state.bookTitle,
        bottomBar = {
            MyReadingBottomBar(
                textSize = state.textSize,
                onSeek = viewModel::onTextSizeChange
            )
        }
    ) {
        MyLazyColumn(
            modifier = Modifier.padding(it),
            lazyList = {
                items(state.doors) { item ->
                    DoorCard(
                        door = item,
                        textSize = state.textSize
                    )
                }
            }
        )
    }
}

@Composable
private fun DoorCard(
    door: BookContent.Chapter.Door,
    textSize: Float
) {
    val textSizeMargin = 15
    MySurface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = door.title,
                modifier = Modifier.padding(10.dp),
                fontSize = (textSize + textSizeMargin).sp,
                fontWeight = FontWeight.Bold
            )

            MyText(
                text = door.text,
                modifier = Modifier.padding(10.dp),
                fontSize = (textSize + textSizeMargin).sp,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}