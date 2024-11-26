package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R

@Composable
fun SearchComponent(
    value: String,
    modifier: Modifier = Modifier,
    hint: String = stringResource(R.string.search),
    onSubmit: () -> Unit = {},
    onValueChange: (String) -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(horizontal = 6.dp),
        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        },
        trailingIcon = {
            if (value != "") {
                IconButton(
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(24.dp)
                    )
                }
            }
        },
        placeholder = {
            MyText(
                text = hint,
                fontSize = 18.sp,
                textColor = MaterialTheme.colorScheme.outline
            )
        },
        singleLine = true,
        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SearchComponent2(
//    initialValue: String,
//    modifier: Modifier = Modifier,
//    hint: String = stringResource(R.string.search),
//    onSubmit: () -> Unit = {},
//    onValueChange: (String) -> Unit = {}
//) {
//    val state = rememberTextFieldState(initialValue)
//    var expanded by remember { mutableStateOf(false) }
//
//    SearchBar(
//        inputField = {
//            SearchBarDefaults.InputField(
//                state = state,
//                onSearch = { expanded = false },
//                expanded = expanded,
//                onExpandedChange = { expanded = it },
//                placeholder = { Text(hint) },
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
//            )
//        },
//        expanded = expanded,
//        onExpandedChange = {},
//        modifier = modifier
//    ) {}
//}