package bassamalim.hidaya

import android.os.Build
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import bassamalim.hidaya.ui.inFromBottom
import bassamalim.hidaya.ui.outToBottom
import bassamalim.hidaya.ui.inFromTop
import bassamalim.hidaya.ui.outToTop
import bassamalim.hidaya.view.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.gson.Gson

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigator(startRoute: String?) {
    val startDest = startRoute ?: bassamalim.hidaya.ui.Screen.Main.route

    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            bassamalim.hidaya.ui.Screen.About.route
        ) {
            AboutUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.AthkarList(
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
            AthkarListUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.AthkarViewer(
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
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.BookChapters(
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.BookSearcher.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BookSearcherUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Books.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BooksUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.BookViewer(
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
            BookViewerUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.DateConverter.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            DateConverterUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.LocationPicker.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            LocationPickerUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Locator(
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(bassamalim.hidaya.ui.Screen.Main.route) {
            MainUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Qibla.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QiblaUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.QuizLobby.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizLobbyUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.QuizResult(
                "{score}", "{questions}", "{chosen_As}"
            ).route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("questions") { type = IntArrType },
                navArgument("chosen_As") { type = IntArrType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizResultUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Quiz.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.QuranSearcher.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuranSearcherUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.QuranViewer(
                "{type}", "{sura_id}", "{page}"
            ).route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("sura_id") { type = NavType.IntType },
                navArgument("page") { type = NavType.IntType }
            ),
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuranViewerUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.RadioClient.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                RadioClientUI(
                    vm = hiltViewModel()
                )
            }
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Settings.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            SettingsUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.TelawatClient(
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
                    nc = navController,
                    vm = hiltViewModel()
                )
            }
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Telawat.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            TelawatUI(
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.TelawatSuar(
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
                nc = navController,
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Tv.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            TvUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.ui.Screen.Welcome.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            WelcomeUI(
                nc = navController,
                vm = hiltViewModel()
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