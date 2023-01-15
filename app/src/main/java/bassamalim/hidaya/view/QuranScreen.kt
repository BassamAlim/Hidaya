package bassamalim.hidaya.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.state.QuranState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.QuranVM

@Composable
fun QuranUI(
    navController: NavController = rememberNavController(),
    viewModel: QuranVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {}
    }

    MyScaffold(
        topBar = {},
        fab = {
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
                description = stringResource(R.string.search_in_quran)
            ) {
                viewModel.onQuranSearcherClick(navController)
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            MyButton(
                text = state.bookmarkedPageText,
                fontSize = 18.sp,
                textColor = AppTheme.colors.accent,
                modifier = Modifier.fillMaxWidth(),
                innerPadding = PaddingValues(vertical = 4.dp)
            ) {
                viewModel.onBookmarkedPageClick(navController)
            }

            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorite)
                ),
                searchComponent = {
                    SearchComponent(
                        value = viewModel.searchText,
                        hint = stringResource(R.string.quran_query_hint),
                        modifier = Modifier.fillMaxWidth(),
                        onSubmit = { viewModel.onSearchSubmit(navController) }
                    ) {
                        viewModel.onSearchTextChange(it)
                    }
                }
            ) { pageNum ->
                viewModel.onListTypeChange(pageNum)

                Tab(
                    viewModel = viewModel,
                    state = state,
                    navController = navController
                )
            }
        }
    }

    TutorialDialog(
        textResId = R.string.quran_fragment_tips,
        shown = state.isTutorialDialogShown
    ) {
        viewModel.onTutorialDialogDismiss(it)
    }

    LaunchedEffect(key1 = state.shouldShowPageDNE) {
        Toast.makeText(
            context,
            context.getString(R.string.page_does_not_exist),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun Tab(
    viewModel: QuranVM,
    state: QuranState,
    navController: NavController
) {
    MyLazyColumn(
        lazyList = {
            items(state.items) { item ->
                MyClickableSurface(
                    onClick = { viewModel.onSuraClick(item.id, navController) }
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
                                if (item.tanzeel == 0) R.drawable.ic_kaaba
                                else R.drawable.ic_madina
                            ),
                            contentDescription = stringResource(R.string.tanzeel_view_description)
                        )

                        MyText(
                            text = item.suraName,
                            modifier = Modifier
                                .weight(1F)
                                .padding(10.dp)
                        )

                        MyFavBtn(state.favs[item.id]) {
                            viewModel.onFavClick(item.id)
                        }
                    }
                }
            }
        }
    )
}