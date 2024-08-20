package bassamalim.hidaya.core.nav

sealed class Screen(val route: String) {

    data object About: Screen("about")

    data class AthkarList(
        val type: String,
        val category: String="-1"
    ): Screen("athkar_list/$type/$category")

    data class AthkarViewer(
        val thikrId: String
    ): Screen("athkar_viewer/$thikrId")

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

    data class TelawatClient(
        val action: String,
        val mediaId: String
    ): Screen("telawat_client/$action/$mediaId")

    data object Telawat: Screen("telawat")

    data class TelawatSuar(
        val reciterId: String,
        val narrationId: String
    ): Screen("telawat_suar/$reciterId/$narrationId")

    data object Tv: Screen("tv")

    data object Welcome: Screen("welcome")

}