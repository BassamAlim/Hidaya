package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.ThikrsDB
import bassamalim.hidaya.models.Thikr
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils

class AthkarViewer : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private lateinit var language: String
    private val infoDialogShown = mutableStateOf(false)
    private var infoDialogText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        db = DBUtils.getDB(this)

        val id = intent.getIntExtra("thikr_id", 0)
        val title =
            if (language == "en") db.athkarDao().getNameEn(id)
            else db.athkarDao().getName(id)

        setContent {
            AppTheme {
                UI(title, id)
            }
        }
    }

    private fun getItems(thikrs: List<ThikrsDB>): List<Thikr> {
        val items = ArrayList<Thikr>()
        for (i in thikrs.indices) {
            val t = thikrs[i]

            if (language == "en" && (t.getTextEn() == null || t.getTextEn()!!.isEmpty())) continue

            if (language == "en")
                items.add(
                    Thikr(
                        t.getThikrId(), t.getTitleEn(), t.getTextEn()!!, t.getTextEnTranslation(),
                        t.getFadlEn(), t.getReferenceEn(), t.getRepetitionEn()
                    )
                )
            else
                items.add(
                    Thikr(t.getThikrId(), t.getTitle(), t.getText()!!,
                        t.getTextEnTranslation(), t.getFadl(), t.getReference(),
                        t.getRepetition()
                    )
                )
        }
        return items
    }

    @Composable
    private fun UI(title: String, id: Int) {
        val textSize = remember {
            mutableStateOf(pref.getInt(getString(R.string.alathkar_text_size_key), 15))
        }

        MyScaffold(
            title = title,
            bottomBar = {
                MyReadingBottomBar(
                    textSizeState = textSize
                ) {
                    textSize.value = it.toInt()

                    pref.edit()
                        .putInt(getString(R.string.alathkar_text_size_key), it.toInt())
                        .apply()
                }
            }
        ) {
            MyLazyColumn(
                modifier = Modifier.padding(it),
                lazyList = {
                    items(
                        items = getItems(db.thikrsDao().getThikrs(id))
                    ) { item ->
                        ThikrCard(thikr = item, textSize)
                    }
                }
            )

            if (infoDialogShown.value)
                InfoDialog(
                    title = stringResource(R.string.reference),
                    text = infoDialogText,
                    infoDialogShown
                )
        }
    }

    @Composable
    private fun ThikrCard(thikr: Thikr, textSize: MutableState<Int>) {
        val textSizeMargin = 15

        MySurface {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (thikr.title != null && thikr.title.isNotEmpty())
                        MyText(
                            text = thikr.title,
                            modifier = Modifier
                                .padding(10.dp),
                            fontSize = (textSize.value + textSizeMargin).sp,
                            fontWeight = FontWeight.Bold
                        )

                    MyText(
                        text = thikr.text,
                        modifier = Modifier
                            .padding(10.dp),
                        fontSize = (textSize.value + textSizeMargin).sp,
                        textColor = AppTheme.colors.strongText
                    )

                    if (language != "ar" &&
                        thikr.textTranslation != null && thikr.textTranslation.isNotEmpty())
                        MyText(
                            text = thikr.textTranslation,
                            modifier = Modifier
                                .padding(10.dp),
                            fontSize = (textSize.value + textSizeMargin).sp
                        )

                    if (thikr.fadl != null && thikr.fadl.isNotEmpty()) {
                        Divider()

                        MyText(
                            text = thikr.fadl,
                            modifier = Modifier
                                .padding(10.dp),
                            fontSize = (textSize.value + textSizeMargin - 8).sp,
                            textColor = AppTheme.colors.accent
                        )
                    }

                    if (thikr.reference != null && thikr.reference.isNotEmpty()) {
                        Divider()

                        MyIconBtn(
                            iconId = R.drawable.ic_help,
                            description = stringResource(R.string.source_btn_description),
                            tint = AppTheme.colors.text
                        ) {
                            infoDialogText = thikr.reference
                            infoDialogShown.value = true
                        }
                    }
                }

                if (thikr.repetition != "1") {
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )

                    MyText(
                        text = thikr.repetition,
                        modifier = Modifier
                            .padding(10.dp)
                            .widthIn(10.dp, 100.dp)
                            .align(Alignment.CenterVertically),
                        fontSize = (textSize.value + textSizeMargin).sp,
                        textColor = AppTheme.colors.accent
                    )
                }
            }
        }
    }

}