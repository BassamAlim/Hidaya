package bassamalim.hidaya

import bassamalim.hidaya.enum.Language.ARABIC
import bassamalim.hidaya.enum.LocationType.Auto
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.enum.Theme.LIGHT
import bassamalim.hidaya.enum.TimeFormat.TWELVE
import bassamalim.hidaya.enum.NotificationType.*

sealed class Prefs(val key: String, val default: Any) {
    object BookmarkedSura: Prefs("bookmarked_sura", -1)
    object Language : Prefs("language_key", ARABIC.name)
    object NumeralsLanguage : Prefs("numerals_language_key", ARABIC.name)
    object TimeFormat : Prefs("time_format_key", TWELVE.name)
    object Theme : Prefs("theme_key", LIGHT.name)
    object LocationType: Prefs("location_type", Auto.name)
    object FirstTime: Prefs("new_user", true)
    object LastDBVersion: Prefs("last_db_version", 1)
    object AthkarTextSize: Prefs("athkar_text_size_key", 15f)
    object BooksTextSize: Prefs("books_text_size_key", 15f)
    object LastDailyUpdate: Prefs("last_daily_update", "No daily updates yet")
    object LastDailyUpdateDay: Prefs("last_daily_update_day", 0)
    object FavoriteSuras: Prefs("favorite_suras", "")
    object FavoriteAthkar: Prefs("favorite_athkar", "")
    object FavoriteReciters: Prefs("favorite_reciters", "")
    object BookmarkedPage: Prefs("bookmarked_page", -1)
    object DateOffset: Prefs("date_offset", 0)
    object CountryID: Prefs("country_id", -1)
    object CityID: Prefs("city_id", -1)
    object StoredLocation: Prefs("stored_location", "")
    object SelectedSearchBooks: Prefs("selected_search_books", "")
    object BookSearcherMaxMatchesIndex: Prefs("books_searcher_max_matches_index", 0)
    object TodayWerdPage: Prefs("today_werd_page", 25)
    object WerdDone: Prefs("werd_done", false)
    object ShowBooksTutorial: Prefs("show_books_tutorial", false)
    object ShowQuranTutorial: Prefs("show_quran_tutorial", false)
    object ShowPrayersTutorial: Prefs("show_prayers_tutorial", false)
    object PrayerTimesCalculationMethod: Prefs("prayer_times_calc_method_key", "MECCA")
    object PrayerTimesJuristicMethod: Prefs("juristic_method_key", "SHAFII")
    object PrayerTimesAdjustment: Prefs("high_lat_adjustment_key", "NONE")
    object TelawatPlaybackRecord: Prefs("telawat_playback_record", 0L)
    object QuranPagesRecord: Prefs("quran_pages_record", 0)
    object AyaReciter: Prefs("aya_reciter", "13")
    object AyaRepeat: Prefs("aya_repeat", 1)
    object StopOnSuraEnd: Prefs("stop_on_sura_end", false)
    object StopOnPageEnd: Prefs("stop_on_page_end", false)
    data class BookChaptersFavs(val bookId: Int): Prefs(
        key = "book${bookId}_favs",
        default = ""
    )
    data class TimeOffset(val pid: PID): Prefs(
        key = "${pid.name}_offset",
        default = 0
    )
    data class NotificationType(val pid: PID): Prefs(
        key = "${pid.name}_notification_type",
        default = if (pid == PID.SUNRISE) None.name else Notification.name
    )
    data class NotifyExtraNotification(val pid: PID): Prefs(
        key = "notify_${pid.name}",
        default = true
    )
    data class ExtraNotificationHour(val pid: PID): Prefs(
        key = "${pid.name}_hour",
        default = when (pid) {
            PID.MORNING -> 5
            PID.EVENING -> 16
            PID.DAILY_WERD -> 21
            PID.FRIDAY_KAHF -> 13
            else -> 0
        }
    )
    data class ExtraNotificationMinute(val pid: PID): Prefs(
        key = "${pid.name}_minute",
        default = 0
    )
}