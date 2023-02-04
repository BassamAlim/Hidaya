package bassamalim.hidaya

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bassamalim.hidaya.view.*

@Composable
fun Navigator(startRoute: String?) {
    val startDest = startRoute ?: Screen.Main.route

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            Screen.About.route
        ) {
            AboutUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.AthkarList(
                "{type}", "{category}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("category") { type = NavType.IntType }
            )
        ) {
            AthkarListUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.AthkarViewer(
                "{thikr_id}"
            ).route,
            arguments = listOf(
                navArgument("thikr_id") { type = NavType.IntType }
            )
        ) {
            AthkarViewerUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.BookChapters(
                "{book_id}", "{book_title}"
            ).route,
            arguments = listOf(
                navArgument("book_id") { type = NavType.IntType },
                navArgument("book_title") { type = NavType.StringType }
            )
        ) {
            BookChaptersUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.BookSearcher.route) {
            BookSearcherUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Books.route) {
            BooksUI(
                navController = navController,
                viewModel = hiltViewModel()
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
            )
        ) {
            BookViewerUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.DateConverter.route) {
            DateConverterUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.LocationPicker.route) {
            LocationPickerUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.Locator(
                "{type}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType }
            )
        ) {
            LocatorUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Main.route) {
            MainUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Qibla.route) {
            QiblaUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.QuizLobby.route) {
            QuizLobbyUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.QuizResult(
                "{score}", "{questions}", "{chosenAs}"
            ).route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("questions") { type = NavType.IntArrayType },
                navArgument("chosenAs") { type = NavType.IntArrayType }
            )
        ) {
            QuizResultUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Quiz.route) {
            QuizUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.QuranSearcher.route) {
            QuranSearcherUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.QuranViewer(
                "{type}", "{suraId}", "{page}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("surah_id") { type = NavType.IntType },
                navArgument("page") { type = NavType.IntType }
            )
        ) {
            QuranViewerUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.RadioClient.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                RadioClientUI(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Splash.route) {
            SplashUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TelawatClient(
                "{action}", "{media_id}"
            ).route,
            arguments = listOf(
                navArgument("action") { type = NavType.StringType },
                navArgument("media_id") { type = NavType.StringType }
            )
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelawatClientUI(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }
        }

        composable(Screen.Telawat.route) {
            TelawatUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TelawatSuar(
                "{reciter_id}", "{version_id}"
            ).route,
            arguments = listOf(
                navArgument("reciter_id") { type = NavType.IntType },
                navArgument("version_id") { type = NavType.IntType }
            )
        ) {
            TelawatSuarUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Tv.route) {
            TvUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}