package bassamalim.hidaya

import android.os.Build
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import bassamalim.hidaya.ui.*
import bassamalim.hidaya.view.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.gson.Gson

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigator(startRoute: String?) {
    val startDest = startRoute ?: bassamalim.hidaya.nav.Screen.Main.route

    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            route = bassamalim.hidaya.nav.Screen.About.route
        ) {
            AboutUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.AthkarList(
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
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.AthkarViewer(
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
            route = bassamalim.hidaya.nav.Screen.BookChapters(
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
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.BookSearcher.route,
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
            route = bassamalim.hidaya.nav.Screen.Books.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            BooksUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.BookViewer(
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
            route = bassamalim.hidaya.nav.Screen.DateConverter.route,
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
            route = bassamalim.hidaya.nav.Screen.LocationPicker.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            LocationPickerUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.Locator(
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
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.Main.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            MainUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.Qibla.route,
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
            route = bassamalim.hidaya.nav.Screen.QuizLobby.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizLobbyUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.QuizResult(
                "{score}", "{questions}", "{chosen_As}"
            ).route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("questions") { type = IntArrType },
                navArgument("chosen_As") { type = IntArrType }
            ),
            enterTransition = inFromLeft,
            exitTransition = outToLeft,
            popEnterTransition = inFromRight,
            popExitTransition = outToBottom
        ) {
            QuizResultUI(
                vm = hiltViewModel()
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.Quiz.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuizUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.QuranSearcher.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            QuranSearcherUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.QuranViewer(
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
            route = bassamalim.hidaya.nav.Screen.RadioClient.route,
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
            route = bassamalim.hidaya.nav.Screen.Settings.route,
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
            route = bassamalim.hidaya.nav.Screen.TelawatClient(
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
            route = bassamalim.hidaya.nav.Screen.Telawat.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            TelawatUI(
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.TelawatSuar(
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
                vm = hiltViewModel(),
                nc = navController
            )
        }

        composable(
            route = bassamalim.hidaya.nav.Screen.Tv.route,
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
            route = bassamalim.hidaya.nav.Screen.Welcome.route,
            enterTransition = inFromBottom,
            exitTransition = outToBottom,
            popEnterTransition = inFromTop,
            popExitTransition = outToTop
        ) {
            WelcomeUI(
                vm = hiltViewModel(),
                nc = navController
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