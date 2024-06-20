package bassamalim.hidaya.features.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val navigator: Navigator
): ViewModel() {

    private val _uiState = MutableStateFlow(MoreState())
    val uiState = _uiState.asStateFlow()

    fun gotoTelawat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            navigator.navigate(Screen.Telawat)
        else {
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
        }
    }

    fun gotoQibla() {
        navigator.navigate(Screen.Qibla)
    }

    fun gotoQuiz() {
        navigator.navigate(Screen.QuizLobby)
    }

    fun gotoBooks() {
        navigator.navigate(Screen.Books)
    }

    fun gotoTV() {
        navigator.navigate(Screen.Tv)
    }

    fun gotoRadio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            navigator.navigate(Screen.RadioClient)
        else {
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
        }
    }

    fun gotoDateConverter() {
        navigator.navigate(Screen.DateConverter)
    }

    fun gotoSettings() {
        navigator.navigate(Screen.Settings)
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

    fun gotoAbout() {
        navigator.navigate(Screen.About)
    }

}