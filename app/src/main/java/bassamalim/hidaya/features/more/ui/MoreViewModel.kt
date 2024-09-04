package bassamalim.hidaya.features.more.ui

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

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState = _uiState.asStateFlow()

    fun onRecitationsClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            navigator.navigate(Screen.RecitationsRecitersMenu)
        else {
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
        }
    }

    fun onQiblaClick() {
        navigator.navigate(Screen.Qibla)
    }

    fun onQuizClick() {
        navigator.navigate(Screen.QuizLobby)
    }

    fun onBooksClick() {
        navigator.navigate(Screen.BooksMenu)
    }

    fun onTvClick() {
        navigator.navigate(Screen.Tv)
    }

    fun onRadioClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            navigator.navigate(Screen.RadioClient)
        else {
            _uiState.update { it.copy(
                shouldShowUnsupported = true
            )}
        }
    }

    fun onDateConverterClick() {
        navigator.navigate(Screen.DateConverter)
    }

    fun onSettingsClick() {
        navigator.navigate(Screen.Settings)
    }

    fun onContactClick(context: Context) {
        val contactIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", Global.CONTACT_EMAIL, null)
        )
        contactIntent.putExtra(Intent.EXTRA_SUBJECT, "Hidaya")
        contactIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(
            Intent.createChooser(
                contactIntent,
                "Choose an Email client :"
            )
        )
    }

    fun onShareClick(context: Context) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App Share")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, Global.PLAY_STORE_URL)
        context.startActivity(
            Intent.createChooser(sharingIntent, "Share via")
        )
    }

    fun onAboutClick() {
        navigator.navigate(Screen.About)
    }

}