@file:OptIn(ExperimentalFoundationApi::class)

package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.core.ui.theme.AppTheme
import com.google.accompanist.pager.pagerTabIndicatorOffset
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
fun Tabs(
    pagerState: PagerState,
    pageNames: List<String>
) {
    // creating a variable for the scope.
    val scope = rememberCoroutineScope()
    // creating a row for our tab layout.
    TabRow(
        // specifying the selected index.
        selectedTabIndex = pagerState.currentPage,
        // on below line we are specifying background color.
        backgroundColor = AppTheme.colors.primary,
        // specifying content color.
        contentColor = Color.White,
        // specifying the indicator for the tab
        indicator = { tabPositions ->
            // specifying the styling for tab indicator by specifying height and color for the tab indicator.
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(
                    pagerState = pagerState,
                    tabPositions = tabPositions
                ),
                height = 2.dp,
                color = AppTheme.colors.secondary
            )
        }
    ) {
        // specifying icon and text for the individual tab item
        pageNames.forEachIndexed { index, _ ->
            // creating a tab.
            Tab(
                text = {
                    MyText(
                        text = pageNames[index],
                        fontSize = 18.sp,
                        textColor =
                            if (pagerState.currentPage == index) AppTheme.colors.secondary
                            else AppTheme.colors.onPrimary
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
fun ColumnScope.TabsContent(
    pagerState: PagerState,
    content: @Composable (Int) -> Unit
) {
    // creating horizontal pager for our tab layout.
    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.weight(1F)
    ) { page ->
        content(page)
    }
}