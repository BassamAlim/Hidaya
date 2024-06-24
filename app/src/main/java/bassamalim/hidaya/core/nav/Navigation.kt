package bassamalim.hidaya.core.nav

import android.os.Build
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bassamalim.hidaya.core.ui.inFromBottom
import bassamalim.hidaya.core.ui.inFromLeft
import bassamalim.hidaya.core.ui.inFromRight
import bassamalim.hidaya.core.ui.inFromTop
import bassamalim.hidaya.core.ui.outToBottom
import bassamalim.hidaya.core.ui.outToLeft
import bassamalim.hidaya.core.ui.outToTop
import bassamalim.hidaya.features.about.ui.AboutScreen
import bassamalim.hidaya.features.supplicationsMenu.AthkarListScreen
import bassamalim.hidaya.features.supplicationsReader.AthkarViewerUI
import bassamalim.hidaya.features.bookChapters.ui.BookChaptersUI
import bassamalim.hidaya.features.bookSearcher.ui.BookSearcherUI
import bassamalim.hidaya.features.bookReader.ui.BookViewerScreen
import bassamalim.hidaya.features.books.ui.BooksUI
import bassamalim.hidaya.features.dateConverter.DateConverterUI
import bassamalim.hidaya.features.hijriDatePicker.HijriDatePickerDialog
import bassamalim.hidaya.features.leaderboard.LeaderboardUI
import bassamalim.hidaya.features.locationPicker.LocationPickerUI
import bassamalim.hidaya.features.locator.LocatorUI
import bassamalim.hidaya.features.main.MainUI
import bassamalim.hidaya.features.prayerReminder.PrayerReminderDialog
import bassamalim.hidaya.features.prayerSetting.PrayerSettingsDialog
import bassamalim.hidaya.features.qibla.QiblaUI
import bassamalim.hidaya.features.quiz.QuizUI
import bassamalim.hidaya.features.quizLobby.QuizLobbyUI
import bassamalim.hidaya.features.quizResult.QuizResultUI
import bassamalim.hidaya.features.quranSearcher.QuranSearcherUI
import bassamalim.hidaya.features.quranReader.QuranViewerUI
import bassamalim.hidaya.features.radio.RadioClientUI
import bassamalim.hidaya.features.settings.SettingsUI
import bassamalim.hidaya.features.recitationsRecitersMenu.TelawatUI
import bassamalim.hidaya.features.recitationsPlayer.TelawatClientUI
import bassamalim.hidaya.features.recitationsSuarMenu.TelawatSuarUI
import bassamalim.hidaya.features.tv.TvUI
import bassamalim.hidaya.features.onboarding.WelcomeUI
import com.google.gson.Gson

@Composable
fun Navigation(
    navigator: Navigator,
    thenTo: String? = null,
    shouldWelcome: Boolean = false
) {
    val navController = rememberNavController()

    LaunchedEffect(key1 = navController) {  // maybe should be DisposableEffect
        navigator.setController(navController)
//        onDispose {
//            navigator.clear()
//        }
    }

    val startDest =
        if (shouldWelcome) Screen.Welcome.route
        else Screen.Main.route

    NavGraph(navController, startDest)

    if (thenTo != null) navController.navigate(thenTo)
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDest: String
) {
    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            route = Screen.About.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            AboutScreen(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.AthkarList(
                "{type}", "{category}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("category") { type = NavType.IntType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            AthkarListScreen(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.AthkarViewer(
                "{thikr_id}"
            ).route,
            arguments = listOf(
                navArgument("thikr_id") { type = NavType.IntType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            AthkarViewerUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.BookChapters(
                "{book_id}", "{book_title}"
            ).route,
            arguments = listOf(
                navArgument("book_id") { type = NavType.IntType },
                navArgument("book_title") { type = NavType.StringType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BookChaptersUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.BookSearcher.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BookSearcherUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Books.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BooksUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.BookViewer(
                "{book_id}", "{book_title}", "{chapter_id}"
            ).route,
            arguments = listOf(
                navArgument("book_id") { type = NavType.IntType },
                navArgument("book_title") { type = NavType.StringType },
                navArgument("chapter_id") { type = NavType.IntType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BookViewerScreen(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.DateConverter.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            DateConverterUI(
                hiltViewModel()
            )
        }

        dialog(
            route = Screen.HijriDatePicker(
                "{initial_date}"
            ).route,
        ) {
            HijriDatePickerDialog(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Leaderboard(
                "{user_id}", "{reading_record}", "{listening_record}"
            ).route,
            arguments = listOf(
                navArgument("user_id") { type = NavType.IntType },
                navArgument("reading_record") { type = NavType.IntType },
                navArgument("listening_record") { type = NavType.LongType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            LeaderboardUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.LocationPicker.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            LocationPickerUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Locator(
                "{type}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            LocatorUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Main.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            MainUI(
                hiltViewModel()
            )
        }

        dialog(
            route = Screen.PrayerReminder(
                "{pid}"
            ).route,
            arguments = listOf(
                navArgument("pid") { type = NavType.StringType }
            )
        ) {
            PrayerReminderDialog(
                hiltViewModel()
            )
        }

        dialog(
            route = Screen.PrayerSettings(
                "{pid}"
            ).route,
            arguments = listOf(
                navArgument("pid") { type = NavType.StringType }
            )
        ) {
            PrayerSettingsDialog(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Qibla.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QiblaUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.QuizLobby.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizLobbyUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.QuizResult(
                "{score}", "{questions}", "{chosen_answers}"
            ).route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("questions") { type = IntArrType },
                navArgument("chosen_answers") { type = IntArrType }
            ),
            enterTransition = inFromLeft,
            exitTransition = outToLeft,
            popEnterTransition = inFromRight,
            popExitTransition = outToBottom
        ) {
            QuizResultUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Quiz.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.QuranSearcher.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuranSearcherUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.QuranViewer(
                "{target_type}", "{target_value}"
            ).route,
            arguments = listOf(
                navArgument("target_type") { type = NavType.StringType },
                navArgument("target_value") { type = NavType.IntType },
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuranViewerUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.RadioClient.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                RadioClientUI(
                    hiltViewModel()
                )
            }
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            SettingsUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.TelawatClient(
                "{action}", "{media_id}"
            ).route,
            arguments = listOf(
                navArgument("action") { type = NavType.StringType },
                navArgument("media_id") { type = NavType.StringType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelawatClientUI(
                    hiltViewModel()
                )
            }
        }

        composable(
            route = Screen.Telawat.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            TelawatUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.TelawatSuar(
                "{reciter_id}", "{version_id}"
            ).route,
            arguments = listOf(
                navArgument("reciter_id") { type = NavType.IntType },
                navArgument("version_id") { type = NavType.IntType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            TelawatSuarUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Tv.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            TvUI(
                hiltViewModel()
            )
        }

        composable(
            route = Screen.Welcome.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            WelcomeUI(
                hiltViewModel()
            )
        }
    }
}


// custom nav type because the default one crashes
val IntArrType: NavType<IntArray> = object : NavType<IntArray>(false) {
    override fun put(bundle: Bundle, key: String, value: IntArray) {
        bundle.putIntArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): IntArray {
        return bundle.getIntArray(key) as IntArray
    }

    override fun parseValue(value: String): IntArray {
        return Gson().fromJson(value, IntArray::class.java)
    }
}