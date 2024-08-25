package bassamalim.hidaya.core.nav

sealed class Screen(val route: String) {

    data object About: Screen("about")

    data class RemembrancesList(
        val type: String,
        val category: String="-1"
    ): Screen("remembrances_list/$type/$category")

    data class RemembranceReader(
        val id: String
    ): Screen("remembrance_reader/$id")

    data class BookChapters(
        val bookId: String,
        val bookTitle: String
    ): Screen("book_chapters/$bookId/$bookTitle")

    data object BookSearcher: Screen("book_searcher")

    data object Books: Screen("books")

    data class BookViewer(
        val bookId: String,
        val bookTitle: String,
        val chapterId: String
    ): Screen("book_viewer/$bookId/$bookTitle/$chapterId")

    data object DateConverter: Screen("date_converter")

    data object DateEditor: Screen("date_editor")

    data class HijriDatePicker(
        val initialDate: String
    ): Screen("hijri_date_picker/$initialDate")

    data object Leaderboard: Screen("leaderboard")

    data object LocationPicker: Screen("location_picker")

    data class Locator(
        val type: String
    ): Screen("locator/$type")

    data object Main: Screen("main")

    data class PrayerReminder(
        val pid: String
    ): Screen("prayer_reminder/$pid")

    data class PrayerSettings(
        val pid: String
    ): Screen("prayer_settings/$pid")

    data object Qibla: Screen("qibla")

    data object QuizLobby: Screen("quiz_lobby")

    data class QuizResult(
        val score: String,
        val questions: String,
        val chosenAnswers: String
    ): Screen("quiz_result/$score/$questions/$chosenAnswers")

    data object Quiz: Screen("quiz")

    data object QuranSearcher: Screen("quran_searcher")

    data class QuranViewer(
        val targetType: String,
        val targetValue: String = "-1",
    ): Screen("quran_viewer/$targetType/$targetValue")

    data object RadioClient: Screen("radio_client")

    data object Settings: Screen("settings")

    data class RecitationsPlayer(
        val action: String,
        val mediaId: String
    ): Screen("recitations_player/$action/$mediaId")

    data object RecitationsRecitersMenu: Screen("recitations_reciters_menu")

    data class RecitationSurasMenu(
        val reciterId: String,
        val narrationId: String
    ): Screen("recitation_suras_menu/$reciterId/$narrationId")

    data object Tv: Screen("tv")

    data object Welcome: Screen("welcome")

}