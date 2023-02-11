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
    val startDest = startRoute ?: Screen.Splash.route

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            Screen.About.route
        ) {
            AboutUI(
                vm = hiltViewModel()
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
                nc = navController,
                vm = hiltViewModel()
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
                nc = navController,
                vm = hiltViewModel()
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.BookSearcher.route) {
            BookSearcherUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.Books.route) {
            BooksUI(
                nc = navController,
                vm = hiltViewModel()
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.DateConverter.route) {
            DateConverterUI(
                vm = hiltViewModel()
            )
        }

        composable(Screen.LocationPicker.route) {
            LocationPickerUI(
                nc = navController,
                vm = hiltViewModel()
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.Main.route) {
            MainUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.Qibla.route) {
            QiblaUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.QuizLobby.route) {
            QuizLobbyUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = Screen.QuizResult(
                "{score}", "{questions}", "{chosen_As}"
            ).route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("questions") {
                    type = NavType.IntArrayType
                    nullable = true
                },
                navArgument("chosen_As") {
                    type = NavType.IntArrayType
                    nullable = true
                }
            )
        ) {
            QuizResultUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.Quiz.route) {
            QuizUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.QuranSearcher.route) {
            QuranSearcherUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = Screen.QuranViewer(
                "{type}", "{sura_id}", "{page}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("sura_id") { type = NavType.IntType },
                navArgument("page") { type = NavType.IntType }
            )
        ) {
            QuranViewerUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.RadioClient.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                RadioClientUI(
                    vm = hiltViewModel()
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsUI(
                vm = hiltViewModel()
            )
        }

        composable(Screen.Splash.route) {
            SplashUI(
                nc = navController,
                vm = hiltViewModel()
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
                    nc = navController,
                    vm = hiltViewModel()
                )
            }
        }

        composable(Screen.Telawat.route) {
            TelawatUI(
                nc = navController,
                vm = hiltViewModel()
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(Screen.Tv.route) {
            TvUI(
                vm = hiltViewModel()
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }
    }
}