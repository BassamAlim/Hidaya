package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R

//@Composable
//fun SearchComponent(
//    value: String,
//    modifier: Modifier = Modifier,
//    hint: String = stringResource(R.string.search),
//    onSubmit: () -> Unit = {},
//    onValueChange: (String) -> Unit = {}
//) {
//    TextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier.padding(horizontal = 6.dp),
//        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
//        leadingIcon = {
//            Icon(
//                imageVector = Icons.Default.Search,
//                contentDescription = "",
//                modifier = Modifier
//                    .padding(horizontal = 10.dp)
//                    .size(24.dp),
//                tint = MaterialTheme.colorScheme.outline
//            )
//        },
//        trailingIcon = {
//            if (value != "") {
//                IconButton(
//                    onClick = { onValueChange("") }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "",
//                        modifier = Modifier
//                            .padding(horizontal = 10.dp)
//                            .size(24.dp)
//                    )
//                }
//            }
//        },
//        placeholder = {
//            MyText(
//                text = hint,
//                fontSize = 18.sp,
//                textColor = MaterialTheme.colorScheme.outline
//            )
//        },
//        singleLine = true,
//        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
//        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
//        colors = TextFieldDefaults.colors(
//            focusedContainerColor = Color.Transparent,
//            unfocusedContainerColor = Color.Transparent
//        )
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = stringResource(R.string.search),
    onSearch: (String) -> Unit = {}
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = false,
                onExpandedChange = {},
                placeholder = { MyText(hint) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier.padding(top = 4.dp, bottom = 6.dp, start = 6.dp, end = 6.dp),
        shape = RoundedCornerShape(10.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        content = {}
    )
}