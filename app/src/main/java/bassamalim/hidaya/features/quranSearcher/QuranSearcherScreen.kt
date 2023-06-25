package bassamalim.hidaya.features.quranSearcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.QuranSearcherMatch
import bassamalim.hidaya.core.ui.components.MyDropDownMenu
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.LangUtils.translateNums

@Composable
fun QuranSearcherUI(
    vm: QuranSearcherVM,
    nc: NavController
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

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
                            st.maxMatches.toString()
                        ),
                        items = vm.translatedMaxMatchesItems
                    ) { index ->
                        vm.onMaxMatchesIndexChange(index)
                    }
                }
            }

            if (st.noResultsFound) {
                MyText(
                    text = stringResource(R.string.no_matches),
                    modifier = Modifier.padding(top = 100.dp)
                )
            }
            else {
                MyLazyColumn(lazyList = {
                    items(st.matches) { item ->
                        MatchItem(item, vm, nc)
                    }
                })
            }
        }
    }
}

@Composable
fun MatchItem(
    item: QuranSearcherMatch,
    vm: QuranSearcherVM,
    nc: NavController
) {
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

            MySquareButton(
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