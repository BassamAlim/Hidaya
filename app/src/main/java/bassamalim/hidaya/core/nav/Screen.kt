package bassamalim.hidaya.core.nav

sealed class Screen(val route: String) {

    data object About: Screen("about")

    data class BookChaptersMenu(
        val bookId: String
    ): Screen("book_chapters_menu/$bookId")

    data class BookReader(
        val bookId: String,
        val chapterId: String
    ): Screen("book_reader/$bookId/$chapterId")

    data object BookSearcher: Screen("book_searcher")

    data object BooksMenu: Screen("books_menu")

    data object BooksMenuFilter: Screen("books_menu_filter")

    data object DateConverter: Screen("date_converter")

    data object DateEditor: Screen("date_editor")

    data class HijriDatePicker(
        val initialDate: String
    ): Screen("hijri_date_picker/$initialDate")

    data object Leaderboard: Screen("leaderboard")

    data object LocationPicker: Screen("location_picker")

    data class Locator(
        val isInitial: String
    ): Screen("locator/$isInitial")

    data object Main: Screen("main")

    data object Onboarding: Screen("onboarding")

    data class PrayerExtraReminderSettings(
        val prayerName: String
    ): Screen("prayer_extra_reminder/$prayerName")

    data class PrayerSettings(
        val prayerName: String
    ): Screen("prayer_settings/$prayerName")

    data object Qibla: Screen("qibla")

    data object QuizLobby: Screen("quiz_lobby")

    data class QuizResult(
        val score: String,
        val questions: String,
        val chosenAnswers: String
    ): Screen("quiz_result/$score/$questions/$chosenAnswers")

    data object QuizTest: Screen("quiz_test")

    data class QuranReader(
        val targetType: String,
        val targetValue: String = "-1",
    ): Screen("quran_reader/$targetType/$targetValue")

    data object QuranSettings: Screen("quran_settings")

    data object Radio: Screen("radio")

    data class RecitationPlayer(
        val action: String,
        val mediaId: String
    ): Screen("recitations_player/$action/$mediaId")

    data object RecitersMenuFilter: Screen("reciters_menu_filter")

    data object RecitationsRecitersMenu: Screen("recitations_reciters_menu")

    data class RecitationSurasMenu(
        val reciterId: String,
        val narrationId: String
    ): Screen("recitation_suras_menu/$reciterId/$narrationId")

    data class RemembranceReader(
        val id: String
    ): Screen("remembrance_reader/$id")

    data class RemembrancesMenu(
        val type: String,
        val categoryId: String="0"
    ): Screen("remembrances_menu/$type/$categoryId")

    data object Settings: Screen("settings")

    data object Tv: Screen("tv")

    data class VerseInfo(
        val verseId: String
    ): Screen("verse_info/$verseId")

}