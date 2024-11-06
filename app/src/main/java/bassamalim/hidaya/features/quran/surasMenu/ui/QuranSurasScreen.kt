package bassamalim.hidaya.features.quran.surasMenu.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyFloatingActionButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

@Composable
fun QuranSurasScreen(viewModel: QuranSurasViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingScreen()
    }
    else {
        MyScaffold(
            title = "",
            topBar = {},  // override the default top bar
            fab = {
                MyFloatingActionButton(
                    iconId = R.drawable.ic_quran_search,
                    description = stringResource(R.string.search_in_quran),
                    onClick = viewModel::onQuranSearcherClick
                )
            }
        ) {
            Column(
                Modifier.fillMaxSize()
            ) {
                MyRectangleButton(
                    text =
                        if (state.bookmarkPageText == null) {
                            stringResource(R.string.no_bookmarked_page)
                        }
                        else {
                            stringResource(R.string.bookmarked_page) +
                                    " ${stringResource(R.string.page)}" +
                                    " ${state.bookmarkPageText}," +
                                    " ${stringResource(R.string.sura)}" +
                                    " ${state.bookmarkSuraText}"
                        },
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    innerPadding = PaddingValues(vertical = 4.dp),
                    onClick = viewModel::onBookmarkedPageClick
                )

                TabLayout(
                    pageNames = listOf(
                        stringResource(R.string.all),
                        stringResource(R.string.favorite)
                    ),
                    searchComponent = {
                        SearchComponent(
                            value = state.searchText,
                            hint = stringResource(R.string.quran_search_hint),
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = viewModel::onSearchTextChange,
                            onSubmit = viewModel::onSearchSubmit
                        )
                    }
                ) { page ->
                    Tab(
                        surasFlow = viewModel.getItems(page),
                        onSuraClick = viewModel::onSuraClick,
                        onFavoriteClick = viewModel::onFavoriteClick
                    )
                }
            }
        }

        TutorialDialog(
            shown = state.isTutorialDialogShown,
            text = stringResource(R.string.quran_fragment_tips),
            onDismiss = viewModel::onTutorialDialogDismiss
        )

        if (state.shouldShowPageDoesNotExist != 0) PageDoesNotExistToast()
    }
}

@Composable
private fun Tab(
    surasFlow: Flow<List<Sura>>,
    onSuraClick: (Int) -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit
) {
    val suras by surasFlow.collectAsStateWithLifecycle(emptyList())

    MyLazyColumn(
        lazyList = {
            items(suras) { item ->
                MyClickableSurface(
                    modifier = Modifier.padding(2.dp),
                    elevation = 6.dp,
                    onClick = { onSuraClick(item.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            top = 10.dp, bottom = 10.dp, start = 14.dp, end = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (item.revelation == 0) R.drawable.ic_kaaba
                                else R.drawable.ic_madina
                            ),
                            contentDescription = stringResource(R.string.revelation_view_description),
                            modifier = Modifier.size(30.dp),
                            tint = AppTheme.colors.shadow
                        )

                        MyText(
                            text = "${stringResource(R.string.sura)} ${item.decoratedName}",
                            modifier = Modifier
                                .weight(1F)
                                .padding(10.dp)
                        )

                        MyFavoriteButton(
                            isFavorite = item.isFavorite,
                            onClick = { onFavoriteClick(item.id, item.isFavorite) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun PageDoesNotExistToast() {
    val context = LocalContext.current
    LaunchedEffect(null) {
        Toast.makeText(
            context,
            context.getString(R.string.page_does_not_exist),
            Toast.LENGTH_SHORT
        ).show()
    }
}