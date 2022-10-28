package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerScreen(
    count: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    content: @Composable (PagerScope.(Int) -> Unit)
) {
    HorizontalPager(
        count = count,
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { currentPage ->
       content(currentPage)
    }
}