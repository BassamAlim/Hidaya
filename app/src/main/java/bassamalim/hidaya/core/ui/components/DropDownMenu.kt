package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun MyDropDownMenu(
    selectedIndex: Int,
    items: Array<String>,
    onChoice: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyText(
                text = items[selectedIndex],
                fontSize = 22.sp,
                modifier = Modifier.padding(horizontal = 5.dp)
            )

            MyIconButton(
                imageVector =
                    if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                description = "Show Dropdown",
                tint = AppTheme.colors.text
            ) {
                expanded = !expanded
            }
        }
        // setting corner width to 0 because there is a problem that shows white corners
        MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(0.dp))) {
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier.background(AppTheme.colors.surface),
                onDismissRequest = { expanded = false }
            ) {
                items.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onChoice(index)
                        }
                    ) {
                        MyText(text = name)
                    }
                }
            }
        }
    }
}

/*@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyDDM(
    title: String,
    selected: MutableState<Int>,
    items: Array<String>,
    onChoice: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // the box
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {

        // text field
        TextField(
            value = selected.value.toString(),
            onValueChange = {},
            readOnly = true,
            label = { MyText(text = title) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        // menu
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { _, name ->
                DropdownMenuItem(
                    onClick = {
                        onChoice(name.toInt())
                        expanded = false
                    }
                ) {
                    MyText(text = name)
                }
            }
        }
    }
}*/

/*@Composable
fun MyDDM2(
    selected: MutableState<Int>,
    items: Array<String>,
    onChoice: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row {
        IconButton(
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(24.dp)
            )
        }

        Column(
            Modifier
                .background(Color.Green)
                .height(IntrinsicSize.Max)
        ) {
            MyText(
                text = selected.value.toString(),
                modifier = Modifier.background(Color.Blue)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppTheme.colors.primary)
            ) {
                items.forEachIndexed { _, name ->
                    DropdownMenuItem(
                        onClick = {
                            onChoice(name.toInt())
                            expanded = false
                        }
                    ) {
                        MyText(text = name)
                    }
                }
            }
        }
    }
}*/

/*
@Composable
fun MySearchableDropDownMenu(
    selected: MutableState<Int>,
    items: Array<String>,
    onChoice: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier.padding(20.dp)
    ) {

        // Create an Outlined Text Field
        // with icon and not expanded
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = { selected.value = it.toInt() },
            modifier = Modifier
                .fillMaxWidth()
                */
/*.onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    mTextFieldSize = coordinates.size.toSize()
                }*//*
,
//            label = {Text("Label")},
            trailingIcon = {
                */
/*MyIconBtn(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    description = "",
                    tint = AppTheme.colors.text,

                ) {
                    expanded = !expanded
                }*//*


                IconButton(
                    onClick = {
                        expanded = !expanded
                    }
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(24.dp)
                    )
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppTheme.colors.weakText,
                focusedBorderColor = AppTheme.colors.accent
            )
        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppTheme.colors.primary)
            */
/*,
            modifier = Modifier
                .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})*//*

        ) {
            items.forEachIndexed { _, name ->
                DropdownMenuItem(
                    onClick = {
                        onChoice(name.toInt())
                        expanded = false
                    }
                ) {
                    MyText(text = name)
                }
            }
        }
    }
}*/
