package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun TabLayout(
    pageNames: List<String>,
    searchComponent: @Composable () -> Unit = {},
    tabsContent: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pageNames.size })

    Column {
        Tabs(
            pagerState = pagerState,
            pageNames = pageNames
        )

        searchComponent()

        TabsContent(
            pagerState = pagerState,
            content = tabsContent
        )
    }
}

@Composable
fun Tabs(pagerState: PagerState, pageNames: List<String>) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = Color.White,
        indicator = { tabPositions ->

            SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                height = 2.dp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    ) {
        pageNames.forEachIndexed { index, _ ->
            Tab(
                text = {
                    MyText(
                        text = pageNames[index],
                        fontSize = 18.sp,
                        textColor =
                            if (pagerState.currentPage == index) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onPrimary
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    // specifying the scope.
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

// creating a tab content method in which we will be displaying the individual page of our tab.
@Composable
fun ColumnScope.TabsContent(pagerState: PagerState, content: @Composable (Int) -> Unit) {
    // creating horizontal pager for our tab layout.
    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.weight(1F)
    ) { page ->
        content(page)
    }
}