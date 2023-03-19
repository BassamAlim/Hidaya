package bassamalim.hidaya.features.quranSearcher

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuranSearcherUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: QuranSearcherVM = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    MyScaffold(stringResource(R.string.quran_searcher)) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val highlightColor = AppTheme.colors.accent

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(stringResource(R.string.search_for_quran_text))

                SearchComponent(
                    value = vm.searchText,
                    hint = stringResource(R.string.search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    vm.onSearchValueChange(it, highlightColor)
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyText(
                        text = stringResource(R.string.max_num_of_marches),
                        fontSize = 18.sp
                    )

                    MyDropDownMenu(
                        selectedIndex = vm.maxMatchesItems.indexOf(
                            state.maxMatches.toString()
                        ),
                        items = vm.translatedMaxMatchesItems
                    ) { index ->
                        vm.onMaxMatchesIndexChange(index)
                    }
                }
            }

            if (state.noResultsFound) {
                MyText(
                    text = stringResource(R.string.no_matches),
                    modifier = Modifier.padding(top = 100.dp)
                )
            }
            else {
                MyLazyColumn(lazyList = {
                    items(state.matches) { item ->
                        MySurface {
                            Column(
                                Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    MyText(
                                        "${stringResource(R.string.sura)} ${item.suraName}"
                                    )

                                    MyText(
                                        "${stringResource(R.string.page)} " +
                                                translateNums(
                                                    vm.numeralsLanguage,
                                                    item.pageNum.toString()
                                                )
                                    )
                                }

                                MyText(
                                    text = "${stringResource(R.string.aya_number)} " +
                                            translateNums(
                                                vm.numeralsLanguage,
                                                item.ayaNum.toString()
                                            ),
                                    modifier = Modifier.padding(6.dp)
                                )
                                MyText(
                                    text = item.text,
                                    modifier = Modifier.padding(6.dp)
                                )
                                MyText(
                                    text = "${stringResource(R.string.tafseer)}: ${item.tafseer}",
                                    modifier = Modifier.padding(6.dp)
                                )

                                MyButton(
                                    text = stringResource(R.string.go_to_page),
                                    textColor = AppTheme.colors.accent,
                                    elevation = 0,
                                    innerPadding = PaddingValues(0.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    vm.onGotoPageClick(item.pageNum, nc)
                                }
                            }
                        }
                    }
                })
            }
        }
    }
}