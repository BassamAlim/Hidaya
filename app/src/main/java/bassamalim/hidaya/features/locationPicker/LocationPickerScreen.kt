package bassamalim.hidaya.features.locationPicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyTopBar
import bassamalim.hidaya.core.ui.components.SearchComponent

@Composable
fun LocationPickerUI(
    vm: LocationPickerVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = "",
        topBar = {
            MyTopBar(
                title = stringResource(st.titleResId),
                onBack = { vm.onBack() }
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchComponent(
                value = vm.searchText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                hint = stringResource(st.searchHintResId),
                onValueChange = { vm.onSearchTextChange(it) }
            )

            MyLazyColumn(
                lazyList = {
                    items(st.items) { item ->
                        MySquareButton(
                            text =
                            if (vm.language == Language.ENGLISH) item.nameEn
                            else item.nameAr,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { vm.onSelect(item.id) }
                        )
                    }
                }
            )
        }
    }
}