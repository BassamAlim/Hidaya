package bassamalim.hidaya

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bassamalim.hidaya.view.*

sealed class Screen(val route: String) {
    object About: Screen("about")

    data class AthkarList(
        val type: String, val category: String=""
    ): Screen("athkar_list/$type/$category")

    data class AthkarViewer(
        val thikrId: String
    ): Screen("athkar_viewer/$thikrId")

    object BookChapters: Screen("book_chapters")

    object BookSearcher: Screen("book_searcher")

    object Books: Screen("books")

    object BookViewer: Screen("book_viewer")

    object DateConverter: Screen("date_converter")

    object LocationPicker: Screen("location_picker")

    object Locator: Screen("locator")

    object Main: Screen("main")

    object Qibla: Screen("qibla")

    object QuizLobby: Screen("quiz_lobby")

    object QuizResult: Screen("quiz_result")

    object Quiz: Screen("quiz")

    object QuranSearcher: Screen("quran_searcher")

    object QuranViewer: Screen("quran_viewer")

    object RadioClient: Screen("radio_client")

    object Settings: Screen("settings")

    object Splash: Screen("splash")

    object TelawatClient: Screen("telawat_client")

    object Telawat: Screen("telawat")

    object TelawatSuar: Screen("telawat_suar")

    object Tv: Screen("tv")

    object Welcome: Screen("welcome")

    fun withArgs(vararg args: String): String {
        return route + args.joinToString(prefix = "/", separator = "/")
    }
}

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
            route = Screen.AthkarList("{type}", "{category}").route,
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
            route = Screen.AthkarViewer("{thikr_id}").route,
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
            route = Screen.BookChapters.route,
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
            route = Screen.BookViewer.route,
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
            route = Screen.Locator.route,
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
            route = Screen.QuizResult.route,
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
            route = Screen.QuranViewer.route,
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
            RadioClientUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
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
            route = Screen.TelawatClient.route,
            arguments = listOf(
                navArgument("action") { type = NavType.StringType },
                navArgument("media_id") { type = NavType.StringType }
            )
        ) {
            TelawatClientUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Telawat.route) {
            TelawatUI(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TelawatSuar.route,
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