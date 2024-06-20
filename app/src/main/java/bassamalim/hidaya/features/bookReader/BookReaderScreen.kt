package bassamalim.hidaya.features.bookReader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyReadingBottomBar
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun BookViewerScreen(
    vm: BookReaderViewModel
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = st.bookTitle,
        bottomBar = {
            MyReadingBottomBar(
                textSize = st.textSize
            ) {
                vm.onTextSizeChange(it)
            }
        }
    ) {
        MyLazyColumn(
            modifier = Modifier.padding(it),
            lazyList = {
                items(st.items) { item ->
                    DoorCard(item, st)
                }
            }
        )
    }
}

@Composable
private fun DoorCard(door: Book.BookChapter.BookDoor, state: BookReaderState) {
    val textSizeMargin = 15
    MySurface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = door.doorTitle,
                modifier = Modifier.padding(10.dp),
                fontSize = (state.textSize + textSizeMargin).sp,
                fontWeight = FontWeight.Bold
            )

            MyText(
                text = door.text,
                modifier = Modifier.padding(10.dp),
                fontSize = (state.textSize + textSizeMargin).sp,
                textColor = AppTheme.colors.strongText
            )
        }
    }
}