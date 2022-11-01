package bassamalim.hidaya.dialogs

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyLazyColumn
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import com.google.gson.Gson

class FilterDialog(
    private val pref: SharedPreferences,
    private val gson: Gson,
    private val title: String,
    private val strings: List<String>,
    private val selected: SnapshotStateList<Boolean>,
    private val filteredState: MutableState<Boolean>,
    private val refresh: (BooleanArray) -> Unit,
    private val prefKey: String,
    private val shown: MutableState<Boolean>
) {

    private fun onDismiss() {
        save()

        setFilterIb()

        shown.value = false
    }

    private fun save() {
        val boolArr = selected.toBooleanArray()

        pref.edit()
            .putString(prefKey, gson.toJson(boolArr))
            .apply()

        refresh(boolArr)
    }

    private fun setFilterIb() {
        var changed = false
        for (bool in selected) {
            if (!bool) {
                changed = true
                break
            }
        }
        filteredState.value = changed
    }

    @Composable
    fun Dialog() {
        MyDialog(shown) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                MyLazyColumn(
                    Modifier.heightIn(0.dp, 300.dp),
                    lazyList = {
                        itemsIndexed(strings) { index, _ ->
                            CheckboxListItem(index = index)
                        }
                    }
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MyText(
                        stringResource(R.string.select_all),
                        textColor = AppTheme.colors.accent,
                        modifier = Modifier.clickable { selected.fill(true) }
                    )

                    MyText(
                        stringResource(R.string.unselect_all),
                        textColor = AppTheme.colors.accent,
                        modifier = Modifier.clickable { selected.fill(false) }
                    )
                }

                MyButton(
                    text = stringResource(id = R.string.select),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    onDismiss()
                }
            }
        }
    }

    @Composable
    private fun CheckboxListItem(index: Int) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = selected[index],
                onCheckedChange = {
                    selected[index] = it
                }
            )

            MyText(strings[index])
        }
    }

}