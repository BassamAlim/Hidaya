package bassamalim.hidaya.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.utils.FileUtils
import bassamalim.hidaya.viewmodel.BooksVM

@Composable
fun BooksUI(
    navController: NavController = rememberNavController(),
    viewModel: BooksVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(key1 = viewModel) {  // Like activity callbacks. Provides onStart, onStop, etc.
        viewModel.onStart()
        onDispose {  /* here we call onStop if needed */  }
    }

    MyScaffold(
        title = stringResource(R.string.hadeeth_books),
        fab = {
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
                description = stringResource(R.string.search_in_books)
            ) {
                viewModel.onFabClick(navController)
            }
        }
    ) {
        MyLazyColumn(
            Modifier.padding(vertical = 5.dp),
            lazyList = {
                items(state.items) { item ->
                    MyBtnSurface(
                        text = item.title,
                        innerVPadding = 15.dp,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(vertical = 2.dp),
                        iconBtn = {
                            MyDownloadBtn(
                                state = viewModel.downloadStates[item.id],
                                path = viewModel.getPath(item.id),
                                modifier = Modifier.padding(end = 10.dp),
                                size = 32.dp,
                                deleted = { viewModel.onFileDeleted(item.id) }
                            ) {
                                viewModel.download(item)
                            }
                        }
                    ) {
                        viewModel.onItemClick(item, navController)
                    }
                }
            }
        )

        TutorialDialog(
            textResId = R.string.books_activity_tips,
            prefKey = "is_first_time_in_books_activity"
        )

        LaunchedEffect(state.shouldShowWaitMassage) {
            FileUtils.showWaitMassage(context)
        }
    }
}