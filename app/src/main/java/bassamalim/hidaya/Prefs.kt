package bassamalim.hidaya

import bassamalim.hidaya.enum.PID

sealed class Prefs(val key: String, val default: Any) {
    object Language : Prefs(
        "language_key",
        bassamalim.hidaya.enum.Language.ARABIC.name
    )
    object NumeralsLanguage : Prefs(
        "numerals_language_key",
        bassamalim.hidaya.enum.Language.ARABIC.name
    )
    object TimeFormat : Prefs(
        "time_format_key",
        bassamalim.hidaya.enum.TimeFormat.TWELVE.name
    )
    object Theme : Prefs(
        "theme_key",
        bassamalim.hidaya.enum.Theme.LIGHT.name
    )
    object LocationType: Prefs(
        "location_type",
        bassamalim.hidaya.enum.LocationType.Auto.name
    )
    object FirstTime: Prefs("new_user", true)
    object LastDBVersion: Prefs("last_db_version", 1)
    object AthkarTextSize: Prefs("athkar_text_size_key", 15f)
    object BooksTextSize: Prefs("books_text_size_key", 15f)
    object LastDailyUpdate: Prefs("last_daily_update", "No daily updates yet")
    object LastDailyUpdateDay: Prefs("last_daily_update_day", 0)
    object FavoriteSuras: Prefs("favorite_suras", "")
    object FavoriteAthkar: Prefs("favorite_athkar", "")
    object FavoriteReciters: Prefs("favorite_reciters", "")
    object DateOffset: Prefs("date_offset", 0)
    object CityID: Prefs("city_id", -1)
    object SelectedSearchBooks: Prefs("selected_search_books", "")
    object BookSearcherMaxMatchesIndex: Prefs("books_searcher_max_matches_index", 0)
    object TodayWerdPage: Prefs("today_werd_page", 25)
    object WerdDone: Prefs("werd_done", false)
    object PrayerTimesCalculationMethod: Prefs("prayer_times_calc_method_key", "MECCA")
    object PrayerTimesJuristicMethod: Prefs("juristic_method_key", "SHAFII")
    object PrayerTimesAdjustment: Prefs("high_lat_adjustment_key", "NONE")
    object TelawatPlaybackRecord: Prefs("telawat_playback_record", 0L)
    object QuranPagesRecord: Prefs("quran_pages_record", 0)
    data class BookChaptersFavs(val bookId: Int): Prefs(
        "book${bookId}_favs",
        ""
    )
    data class PrayerOffset(val pid: PID): Prefs(
        "book${pid.name}_offset",
        0
    )
}