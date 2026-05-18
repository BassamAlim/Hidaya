package bassamalim.hidaya.features.quran.verseInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.QuranBookmarks
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.Bookmark1Color
import bassamalim.hidaya.core.ui.theme.Bookmark2Color
import bassamalim.hidaya.core.ui.theme.Bookmark3Color
import bassamalim.hidaya.core.ui.theme.Bookmark4Color
import bassamalim.hidaya.core.ui.theme.hafs_smart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseInfoDialog(viewModel: VerseInfoViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ModalBottomSheet(onDismissRequest = viewModel::onDismiss) {
        SheetContent(viewModel, state)
    }
}

@Composable
private fun SheetContent(viewModel: VerseInfoViewModel, state: VerseInfoUiState) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BookmarkOptionsSection(
            verseId = state.verseId,
            bookmarks = state.bookmarks,
            onBookmarkClick = viewModel::onBookmarkClick
        )

        MyHorizontalDivider()

        VerseTextSection(state.verseText)

        MyHorizontalDivider()

        InterpretationSection(state.interpretation)
    }
}

@Composable
private fun BookmarkOptionsSection(
    verseId: Int,
    bookmarks: QuranBookmarks,
    onBookmarkClick: (Int, Int?) -> Unit
) {
    val bookmarkVerseIds = listOf(
        bookmarks.bookmark1VerseId,
        bookmarks.bookmark2VerseId,
        bookmarks.bookmark3VerseId,
        bookmarks.bookmark4VerseId
    )
    val bookmarkColors = listOf(Bookmark1Color, Bookmark2Color, Bookmark3Color, Bookmark4Color)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bookmarkVerseIds.forEachIndexed { i, bookmarkVerseId ->
            MyIconButton(
                imageVector =
                    if (verseId == bookmarkVerseId) Icons.Default.BookmarkRemove
                    else Icons.Default.BookmarkAdd,
                description = stringResource(R.string.bookmark_verse_button_description),
                onClick = { onBookmarkClick(i + 1, bookmarkVerseId) },
                iconModifier = Modifier.size(32.dp),
                iconColor = bookmarkColors[i]
            )
        }
    }
}

@Composable
private fun VerseTextSection(verseText: String) {
    MyText(text = verseText, fontFamily = hafs_smart, textAlign = TextAlign.Center)
}

@Composable
private fun InterpretationSection(interpretation: AnnotatedString) {
    Column {
        MyText(
            text = stringResource(R.string.interpretation),
            modifier = Modifier.padding(bottom = 6.dp),
            fontWeight = FontWeight.Bold
        )

        MyText(
            text = interpretation,
            modifier = Modifier.padding(bottom = 6.dp),
            textAlign = TextAlign.Justify
        )

        MyText(
            text = "- ${stringResource(R.string.interpretation_source)}",
            modifier = Modifier.padding(bottom = 6.dp),
            fontSize = 14.sp
        )
    }
}