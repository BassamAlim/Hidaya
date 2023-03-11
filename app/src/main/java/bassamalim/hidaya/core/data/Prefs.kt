package bassamalim.hidaya.core.data

import bassamalim.hidaya.core.enums.Language.ARABIC
import bassamalim.hidaya.core.enums.LocationType.Auto
import bassamalim.hidaya.core.enums.NotificationType.None
import bassamalim.hidaya.core.enums.NotificationType.Notification
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.QViewType
import bassamalim.hidaya.core.enums.Theme.LIGHT
import bassamalim.hidaya.core.enums.TimeFormat.TWELVE

sealed class Prefs(val key: String, val default: Any) {
    object BookmarkedSura: bassamalim.hidaya.core.data.Prefs("bookmarked_sura", -1)
    object Language : bassamalim.hidaya.core.data.Prefs("language_key", ARABIC.name)
    object NumeralsLanguage : bassamalim.hidaya.core.data.Prefs("numerals_language_key", ARABIC.name)
    object TimeFormat : bassamalim.hidaya.core.data.Prefs("time_format_key", TWELVE.name)
    object Theme : bassamalim.hidaya.core.data.Prefs("theme_key", LIGHT.name)
    object LocationType: bassamalim.hidaya.core.data.Prefs("location_type", Auto.name)
    object FirstTime: bassamalim.hidaya.core.data.Prefs("new_user", true)
    object LastDBVersion: bassamalim.hidaya.core.data.Prefs("last_db_version", 1)
    object LastPlayedMediaId: bassamalim.hidaya.core.data.Prefs("last_played_media_id", "")
    object AthkarTextSize: bassamalim.hidaya.core.data.Prefs("athkar_text_size_key", 15f)
    object BooksTextSize: bassamalim.hidaya.core.data.Prefs("books_text_size_key", 15f)
    object LastDailyUpdate: bassamalim.hidaya.core.data.Prefs("last_daily_update", "No daily updates yet")
    object LastDailyUpdateDay: bassamalim.hidaya.core.data.Prefs("last_daily_update_day", 0)
    object FavoriteSuras: bassamalim.hidaya.core.data.Prefs("favorite_suras", "")
    object FavoriteAthkar: bassamalim.hidaya.core.data.Prefs("favorite_athkar", "")
    object FavoriteReciters: bassamalim.hidaya.core.data.Prefs("favorite_reciters", "")
    object BookmarkedPage: bassamalim.hidaya.core.data.Prefs("bookmarked_page", -1)
    object DateOffset: bassamalim.hidaya.core.data.Prefs("date_offset", 0)
    object CountryID: bassamalim.hidaya.core.data.Prefs("country_id", -1)
    object CityID: bassamalim.hidaya.core.data.Prefs("city_id", -1)
    object SelectedSearchBooks: bassamalim.hidaya.core.data.Prefs("selected_search_books", "")
    object BookSearcherMaxMatchesIndex: bassamalim.hidaya.core.data.Prefs("books_searcher_max_matches_index", 0)
    object QuranSearcherMaxMatchesIndex: bassamalim.hidaya.core.data.Prefs("quran_searcher_max_matches_index", 0)
    object TodayWerdPage: bassamalim.hidaya.core.data.Prefs("today_werd_page", 25)
    object WerdDone: bassamalim.hidaya.core.data.Prefs("werd_done", false)
    object ShowBooksTutorial: bassamalim.hidaya.core.data.Prefs("show_books_tutorial", true)
    object ShowQuranTutorial: bassamalim.hidaya.core.data.Prefs("show_quran_tutorial", true)
    object ShowQuranViewerTutorial: bassamalim.hidaya.core.data.Prefs("show_quran_viewer_tutorial", true)
    object ShowPrayersTutorial: bassamalim.hidaya.core.data.Prefs("show_prayers_tutorial", true)
    object StoredLocation: bassamalim.hidaya.core.data.Prefs("stored_location", "{}")
    object SelectedRewayat: bassamalim.hidaya.core.data.Prefs("selected_rewayat", "")
    object PrayerTimesCalculationMethod: bassamalim.hidaya.core.data.Prefs("prayer_times_calc_method_key", "MECCA")
    object PrayerTimesJuristicMethod: bassamalim.hidaya.core.data.Prefs("juristic_method_key", "SHAFII")
    object PrayerTimesAdjustment: bassamalim.hidaya.core.data.Prefs("high_lat_adjustment_key", "NONE")
    object TelawatPlaybackRecord: bassamalim.hidaya.core.data.Prefs("telawat_playback_record", 0L)
    object TelawatRepeatMode: bassamalim.hidaya.core.data.Prefs("telawat_repeat_mode", 0)
    object TelawatShuffleMode: bassamalim.hidaya.core.data.Prefs("telawat_shuffle_mode", 0)
    object QuranPagesRecord: bassamalim.hidaya.core.data.Prefs("quran_pages_record", 0)
    object QuranTextSize: bassamalim.hidaya.core.data.Prefs("quran_text_size", 30f)
    object QuranViewType: bassamalim.hidaya.core.data.Prefs("quran_view_type", QViewType.Page.name)
    object AyaReciter: bassamalim.hidaya.core.data.Prefs("aya_reciter", "13")
    object AyaRepeat: bassamalim.hidaya.core.data.Prefs("aya_repeat", 1f)
    object StopOnSuraEnd: bassamalim.hidaya.core.data.Prefs("stop_on_sura_end", false)
    object StopOnPageEnd: bassamalim.hidaya.core.data.Prefs("stop_on_page_end", false)
    data class BookChaptersFavs(val bookId: Int): bassamalim.hidaya.core.data.Prefs(
        key = "book${bookId}_favs",
        default = ""
    )
    data class TimeOffset(val pid: PID): bassamalim.hidaya.core.data.Prefs(
        key = "${pid.name}_offset",
        default = 0
    )
    data class NotificationType(val pid: PID): bassamalim.hidaya.core.data.Prefs(
        key = "${pid.name}_notification_type",
        default = if (pid == PID.SUNRISE) None.name else Notification.name
    )
    data class NotifyExtraNotification(val pid: PID): bassamalim.hidaya.core.data.Prefs(
        key = "notify_${pid.name}",
        default = true
    )
    data class ExtraNotificationHour(val pid: PID): bassamalim.hidaya.core.data.Prefs(
        key = "${pid.name}_hour",
        default = when (pid) {
            PID.MORNING -> 5
            PID.EVENING -> 16
            PID.DAILY_WERD -> 21
            PID.FRIDAY_KAHF -> 10
            else -> 0
        }
    )
    data class ExtraNotificationMinute(val pid: PID): bassamalim.hidaya.core.data.Prefs(
        key = "${pid.name}_minute",
        default = 0
    )
}