package bassamalim.hidaya.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.state.BookViewerState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.BookViewerVM

@Composable
fun BookViewerUI(
    navController: NavController = rememberNavController(),
    viewModel: BookViewerVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(
        title = viewModel.bookTitle,
        bottomBar = {
            MyReadingBottomBar(
                textSize = state.textSize
            ) {
                viewModel.onTextSizeChange(it)
            }
        }
    ) {
        MyLazyColumn(
            modifier = Modifier.padding(it),
            lazyList = {
                items(state.items) { item ->
                    DoorCard(item, state)
                }
            }
        )
    }
}

@Composable
private fun DoorCard(door: Book.BookChapter.BookDoor, state: BookViewerState) {
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