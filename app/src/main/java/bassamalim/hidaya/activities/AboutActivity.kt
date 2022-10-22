package bassamalim.hidaya.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyHorizontalDivider
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun UI() {
        MyScaffold(stringResource(id = R.string.about)) {
            var counter by remember { mutableStateOf(0) }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(horizontal = 5.dp)
            ) {
                MyText(
                    text = stringResource(id = R.string.thanks),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 15.dp, bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (++counter == 5)
                                Toast
                                    .makeText(
                                        this@AboutActivity,
                                        getString(R.string.vip_welcome), Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                )

                Column(
                    Modifier
                        .weight(1F)
                        .verticalScroll(rememberScrollState())
                ) {
                    Source(R.string.quran_source)
                    MyHorizontalDivider()
                    Source(R.string.tafseer_source)
                    MyHorizontalDivider()
                    Source(R.string.hadeeth_source)
                    MyHorizontalDivider()
                    Source(R.string.athkar_source)
                    MyHorizontalDivider()
                    Source(R.string.quiz_source)
                }

                MyButton(
                    stringResource(id = R.string.rebuild_database),
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .alpha(if (counter < 5) 0F else 1F)
                ) {
                    deleteDatabase("HidayaDB")

                    Log.i(Global.TAG, "Database Rebuilt")

                    DBUtils.reviveDB(this@AboutActivity)

                    Toast.makeText(
                        this@AboutActivity, getString(R.string.database_rebuilt),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                MyButton(
                    stringResource(id = R.string.quick_update),
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .alpha(if (counter < 5) 0F else 1F)
                ) {
                    val url = FirebaseRemoteConfig.getInstance().getString(Global.UPDATE_URL)
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
                MyText(
                    text =
                        if (counter < 5) ""
                        else PreferenceManager.getDefaultSharedPreferences(
                            this@AboutActivity
                        ).getString("last_daily_update", "No daily updates yet")!!,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp)
                )
            }
        }
    }

    @Composable
    private fun Source(textResId: Int) {
        MyText(
            text = stringResource(id = textResId),
            modifier = Modifier.padding(10.dp),
            fontSize = 22.sp,
            textAlign = TextAlign.Start
        )
    }

}

