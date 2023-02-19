package bassamalim.hidaya.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.state.MoreState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MoreVM @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(MoreState())
    val uiState = _uiState.asStateFlow()

    fun gotoTelawat(nc: NavController) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            nc.navigate(Screen.Telawat.route)
        else
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
    }

    fun gotoQibla(nc: NavController) {
        nc.navigate(Screen.Qibla.route)
    }

    fun gotoQuiz(nc: NavController) {
        nc.navigate(Screen.QuizLobby.route)
    }

    fun gotoBooks(nc: NavController) {
        nc.navigate(Screen.Books.route)
    }

    fun gotoTV(nc: NavController) {
        nc.navigate(Screen.Tv.route)
    }

    fun gotoRadio(nc: NavController) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            nc.navigate(Screen.RadioClient.route)
        else
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
    }

    fun gotoDateConverter(nc: NavController) {
        nc.navigate(Screen.DateConverter.route)
    }

    fun gotoSettings(nc: NavController) {
        nc.navigate(Screen.Settings.route)
    }

    fun contactMe(ctx: Context) {
        val contactIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", Global.CONTACT_EMAIL, null)
        )
        contactIntent.putExtra(Intent.EXTRA_SUBJECT, "Hidaya")
        contactIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(
            Intent.createChooser(
                contactIntent,
                "Choose an Email client :"
            )
        )
    }

    fun shareApp(ctx: Context) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App Share")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, Global.PLAY_STORE_URL)
        ctx.startActivity(
            Intent.createChooser(sharingIntent, "Share via")
        )
    }

    fun gotoAbout(nc: NavController) {
        nc.navigate(Screen.About.route)
    }

}