package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils

class QuizLobbyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    @Composable
    private fun UI() {
        MyScaffold(stringResource(R.string.quiz_title)) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyButton(
                    text = stringResource(R.string.start_quiz),
                    innerPadding = PaddingValues(vertical = 10.dp, horizontal = 25.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textColor = AppTheme.colors.accent
                ) {
                    startActivity(Intent(
                        this@QuizLobbyActivity,
                        QuizActivity::class.java
                    ))
                }
            }
        }
    }

}