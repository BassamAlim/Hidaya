package bassamalim.hidaya.core.data.preferences

import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.Language as Languages
import bassamalim.hidaya.core.enums.LocationType as LocationTypes
import bassamalim.hidaya.core.enums.NotificationType as NotificationTypes
import bassamalim.hidaya.core.enums.QuranViewType as QuranViewTypes
import bassamalim.hidaya.core.enums.Theme as Themes
import bassamalim.hidaya.core.enums.TimeFormat as TimeFormats

sealed class Preference(val key: String, val default: Any) {

    // PrayersPreferences
    data object AthanVoice : Preference("athan_voice", "1")

    // SupplicationsPreferences
    data object AthkarTextSize : Preference("athkar_text_size_key", 15f)

    // QuranPreferences
    data object AyaReciter : Preference("aya_reciter", "13")

    // QuranPreferences
    data object AyaRepeat : Preference("aya_repeat", 1f)  // float to use in slider

    // BooksPreferences
    data object BooksTextSize : Preference("books_text_size_key", 15f)

    // QuranPreferences
    data object BookmarkedPage : Preference("bookmarked_page", -1)

    // QuranPreferences
    data object BookmarkedSura : Preference("bookmarked_sura", -1)

    // BooksPreferences
    data object BookSearcherMaxMatchesIndex : Preference(
        "books_searcher_max_matches_index",
        0
    )

    // BooksPreferences
    data class BookChaptersFavs(val bookId: Int) : Preference(
        key = "book${bookId}_favs",
        default = ""
    )

    // LocationPreferences
    data object CountryID : Preference("country_id", -1)

    // LocationPreferences
    data object CityID : Preference("city_id", -1)

    // AppSettingsPreferences
    data object DateOffset : Preference("date_offset", 0)

    // NotificationsPreferences
    data class ExtraNotificationMinuteOfDay(val pid: PID) : Preference(
        key = "${pid.name}_mod",
        default = when (pid) {
            PID.MORNING -> 300  // 5 am
            PID.EVENING -> 960  // 4 pm
            PID.DAILY_WERD -> 1260  // 9 pm
            PID.FRIDAY_KAHF -> 600 // 10 am
            else -> 0
        }
    )

    // QuranPreferences
    data object FavoriteSuar : Preference("favorite_suar", "")

    // SupplicationsPreferences
    data object FavoriteAthkar : Preference("favorite_athkar", "")

    // RecitationsPreferences
    data object FavoriteReciters : Preference("favorite_reciters", "")

    // AppStatePreferences
    data object FirstTime : Preference("new_user", true)

    // AppSettingsPreferences
    data object Language : Preference("language_key", Languages.ARABIC.name)

    // AppStatePreferences
    data object LastDailyUpdateMillis : Preference("last_daily_update_millis", 0L)

    // LocationPreferences
    data object LocationType : Preference("location_type", LocationTypes.Auto.name)

    // AppStatePreferences
    data object LastDBVersion : Preference("last_db_version", 1)

    // NotificationsPreferences
    data class LastNotificationDate(val pid: PID) : Preference(
        key = "last_${pid.name}_notification_date",
        default = -1
    )

    // RecitationsPreferences
    data object LastPlayedMediaId : Preference("last_played_media_id", "")

    // AppSettingsPreferences
    data object NumeralsLanguage : Preference("numerals_language_key", Languages.ARABIC.name)

    // NotificationsPreferences
    data class NotificationType(val pid: PID) : Preference(
        key = "${pid.name}_notification_type",
        default =
            if (pid == PID.SUNRISE) NotificationTypes.None.name
            else NotificationTypes.Notification.name
    )

    // NotificationsPreferences
    data class NotifyExtraNotification(val pid: PID) : Preference(
        key = "notify_${pid.name}",
        default = true
    )

    // PrayersPreferences
    data object PrayerTimesCalculationMethod : Preference(
        "prayer_times_calc_method_key",
        "MECCA"
    )

    // PrayersPreferences
    data object PrayerTimesJuristicMethod : Preference("juristic_method_key", "SHAFII")

    // PrayersPreferences
    data object PrayerTimesAdjustment : Preference("high_lat_adjustment_key", "NONE")

    // QuranPreferences
    data object QuranSearcherMaxMatchesIndex : Preference(
        "quran_searcher_max_matches_index",
        0
    )

    // UserPreferences
    data object QuranPagesRecord : Preference("quran_pages_record", 0)

    // QuranPreferences
    data object QuranTextSize : Preference("quran_text_size", 30f)

    // QuranPreferences
    data object QuranViewType : Preference("quran_view_type", QuranViewTypes.PAGE.name)

    // QuranPreferences
    data object StopOnSuraEnd : Preference("stop_on_sura_end", false)

    // QuranPreferences
    data object StopOnPageEnd : Preference("stop_on_page_end", false)

    // BooksPreferences
    data object ShowBooksTutorial : Preference("show_books_tutorial", true)

    // QuranPreferences
    data object ShowQuranTutorial : Preference("show_quran_tutorial", true)

    // QuranPreferences
    data object ShowQuranViewerTutorial : Preference("show_quran_viewer_tutorial", true)

    // PrayersPreferences
    data object ShowPrayersTutorial : Preference("show_prayers_tutorial", true)

    // LocationPreferences
    data object StoredLocation : Preference("stored_location", "{}")

    // BooksPreferences
    data object SelectedSearchBooks : Preference("selected_search_books", "")

    // RecitationsPreferences
    data object SelectedRewayat : Preference("selected_rewayat", "")

    // UserPreferences
    data object TelawatPlaybackRecord : Preference("telawat_playback_record", 0L)

    // RecitationsPreferences
    data object TelawatRepeatMode : Preference("telawat_repeat_mode", 0)

    // RecitationsPreferences
    data object TelawatShuffleMode : Preference("telawat_shuffle_mode", 0)

    // AppSettingsPreferences
    data object TimeFormat : Preference("time_format_key", TimeFormats.TWELVE.name)

    // AppSettingsPreferences
    data object Theme : Preference("theme_key", Themes.LIGHT.name)

    // PrayersPreferences
    data class TimeOffset(val pid: PID) : Preference(key = "${pid.name}_offset", default = 0)

    // NotificationsPreferences
    data class ReminderOffset(val pid: PID) : Preference(
        key = "${pid.name}_reminder_offset",
        default = 0
    )

    // QuranPreferences
    data object WerdPage : Preference("werd_page", 25)

    // QuranPreferences
    data object WerdDone : Preference("werd_done", false)

    // RecitationsPreferences
    data object LastTelawaProgress : Preference("last_telawa_progress", 0)

}