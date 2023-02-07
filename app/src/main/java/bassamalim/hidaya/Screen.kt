package bassamalim.hidaya

sealed class Screen(val route: String) {
    object About: Screen("about")

    data class AthkarList(
        val type: String, val category: String="-1"
    ): Screen("athkar_list/$type/$category")

    data class AthkarViewer(
        val thikrId: String
    ): Screen("athkar_viewer/$thikrId")

    data class BookChapters(
        val bookId: String, val bookTitle: String
    ): Screen("book_chapters/$bookId/$bookTitle")

    object BookSearcher: Screen("book_searcher")

    object Books: Screen("books")

    data class BookViewer(
        val bookId: String, val bookTitle: String, val chapterId: String
    ): Screen("book_viewer/$bookId/$bookTitle/$chapterId")

    object DateConverter: Screen("date_converter")

    object LocationPicker: Screen("location_picker")

    data class Locator(
        val type: String
    ): Screen("locator/$type")

    object Main: Screen("main")

    object Qibla: Screen("qibla")

    object QuizLobby: Screen("quiz_lobby")

    data class QuizResult(
        val score: String, val questions: String, val chosenAs: String
    ): Screen("quiz_result/$score/$questions/$chosenAs")

    object Quiz: Screen("quiz")

    object QuranSearcher: Screen("quran_searcher")

    data class QuranViewer(
        val type: String, val suraId: String = "-1", val page: String = "-1"
    ): Screen("quran_viewer/$type/$suraId/$page")

    object RadioClient: Screen("radio_client")

    object Settings: Screen("settings")

    object Splash: Screen("splash")

    data class TelawatClient(
        val action: String, val mediaId: String
    ): Screen("telawat_client/$action/$mediaId")

    object Telawat: Screen("telawat")

    data class TelawatSuar(
        val reciterId: String, val versionId: String
    ): Screen("telawat_suar/$reciterId/$versionId")

    object Tv: Screen("tv")

    object Welcome: Screen("welcome")
}