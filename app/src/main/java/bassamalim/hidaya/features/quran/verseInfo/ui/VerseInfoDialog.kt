package bassamalim.hidaya.features.quran.verseInfo.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.QuranBookmarks
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
            onBookmark1Click = viewModel::onBookmark1Click,
            onBookmark2Click = viewModel::onBookmark2Click,
            onBookmark3Click = viewModel::onBookmark3Click,
            onBookmark4Click = viewModel::onBookmark4Click
        )

        VerseTextSection(state.verseText)

        InterpretationSection(state.interpretation)
    }
}

@Composable
private fun VerseTextSection(verseText: String) {
    MyText(
        text = verseText,
        modifier = Modifier
            .padding(6.dp),
        fontFamily = hafs_smart,
        textAlign = TextAlign.Center,
        textColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun InterpretationSection(interpretation: AnnotatedString) {
    Column {
        MyText(
            text = stringResource(R.string.interpretation),
            modifier = Modifier.padding(bottom = 6.dp),
            fontWeight = FontWeight.Bold,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        MyText(
            text = interpretation,
            modifier = Modifier.padding(6.dp),
            fontFamily = hafs_smart,
            textAlign = TextAlign.Justify,
            textColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BookmarkOptionsSection(
    verseId: Int,
    bookmarks: QuranBookmarks,
    onBookmark1Click: (Int?) -> Unit,
    onBookmark2Click: (Int?) -> Unit,
    onBookmark3Click: (Int?) -> Unit,
    onBookmark4Click: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MyIconButton(
            imageVector =
                if (verseId == bookmarks.bookmark1VerseId) Icons.Default.BookmarkRemove
                else Icons.Default.BookmarkAdd,
            description = stringResource(R.string.bookmark_verse_button_description),
            onClick = { onBookmark1Click(bookmarks.bookmark1VerseId) },
            iconModifier = Modifier.size(32.dp),
            contentColor = Bookmark1Color
        )

        MyIconButton(
            imageVector =
                if (verseId == bookmarks.bookmark2VerseId) Icons.Default.BookmarkRemove
                else Icons.Default.BookmarkAdd,
            description = stringResource(R.string.bookmark_verse_button_description),
            onClick = { onBookmark2Click(bookmarks.bookmark2VerseId) },
            iconModifier = Modifier.size(32.dp),
            contentColor = Bookmark2Color
        )

        MyIconButton(
            imageVector =
                if (verseId == bookmarks.bookmark3VerseId) Icons.Default.BookmarkRemove
                else Icons.Default.BookmarkAdd,
            description = stringResource(R.string.bookmark_verse_button_description),
            onClick = { onBookmark3Click(bookmarks.bookmark3VerseId) },
            iconModifier = Modifier.size(32.dp),
            contentColor = Bookmark3Color
        )

        MyIconButton(
            imageVector =
                if (verseId == bookmarks.bookmark4VerseId) Icons.Default.BookmarkRemove
                else Icons.Default.BookmarkAdd,
            description = stringResource(R.string.bookmark_verse_button_description),
            onClick = { onBookmark4Click(bookmarks.bookmark4VerseId) },
            iconModifier = Modifier.size(32.dp),
            contentColor = Bookmark4Color
        )
    }
}