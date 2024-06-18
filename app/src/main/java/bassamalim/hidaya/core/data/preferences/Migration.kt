package bassamalim.hidaya.core.data.preferences

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.dataStore.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.dataStore.objects.AppSettingsPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.AppStatePreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.BooksPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.NotificationsPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.PrayersPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.QuranPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.RecitationsPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.SupplicationsPreferences
import bassamalim.hidaya.core.data.preferences.dataStore.objects.UserPreferences
import bassamalim.hidaya.core.enums.AyaRepeat
import bassamalim.hidaya.core.enums.HighLatAdjustmentMethod
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PTCalculationMethod
import bassamalim.hidaya.core.enums.PTJuristicMethod
import bassamalim.hidaya.core.enums.QuranViewType
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.MyLocation
import com.google.gson.Gson
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

object Migrations {

    fun getAppSettingsPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.APP_SETTINGS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: AppSettingsPreferences ->
            currentData.copy(
                language = Language.valueOf(
                    sharedPrefs.getString(
                        key = Preference.Language.key,
                        defValue = Preference.Language.default as String
                    )!!
                ),
                numeralsLanguage = Language.valueOf(
                    sharedPrefs.getString(
                        key = Preference.NumeralsLanguage.key,
                        defValue = Preference.NumeralsLanguage.default as String
                    )!!
                ),
                timeFormat = TimeFormat.valueOf(
                    sharedPrefs.getString(
                        key = Preference.TimeFormat.key,
                        defValue = Preference.TimeFormat.default as String
                    )!!
                ),
                theme = Theme.valueOf(
                    sharedPrefs.getString(
                        key = Preference.Theme.key,
                        defValue = Preference.Theme.default as String
                    )!!
                ),
                dateOffset = sharedPrefs.getInt(
                    key = Preference.DateOffset.key,
                    defValue = Preference.DateOffset.default as Int
                ),
            )
        }
    }

    fun getAppStatePreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.APP_STATE_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: AppStatePreferences ->
            currentData.copy(
                isOnboardingCompleted = !sharedPrefs.getBoolean(
                    key = Preference.FirstTime.key,
                    defValue = Preference.FirstTime.default as Boolean
                ),
                lastDailyUpdateMillis = 0,
                lastDBVersion = sharedPrefs.getInt(
                    key = Preference.LastDBVersion.key,
                    defValue =  Preference.LastDBVersion.default as Int
                ),
            )
        }
    }

    fun getBooksPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.BOOKS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: BooksPreferences ->
            currentData.copy(
                textSize = sharedPrefs.getFloat(
                    key = Preference.BooksTextSize.key,
                    defValue = Preference.BooksTextSize.default as Float
                ),
                searcherMaxMatches = 10,
                chaptersFavs = persistentMapOf(),
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowBooksTutorial.key,
                    defValue = Preference.ShowBooksTutorial.default as Boolean
                ),
                searchSelections = persistentMapOf(),
            )
        }
    }

    fun getNotificationsPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.NOTIFICATIONS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: NotificationsPreferences ->
            currentData.copy(
                extraNotificationsMinuteOfDay = persistentMapOf(
                    PID.MORNING to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.MORNING).key,
                        defValue = Preference.ExtraNotificationHour(PID.MORNING).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.MORNING).key,
                        defValue = Preference.ExtraNotificationMinute(PID.MORNING).default as Int
                    ),
                    PID.EVENING to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.EVENING).key,
                        defValue = Preference.ExtraNotificationHour(PID.EVENING).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.EVENING).key,
                        defValue = Preference.ExtraNotificationMinute(PID.EVENING).default as Int
                    ),
                    PID.DAILY_WERD to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.DAILY_WERD).key,
                        defValue = Preference.ExtraNotificationHour(PID.DAILY_WERD).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.DAILY_WERD).key,
                        defValue = Preference.ExtraNotificationMinute(PID.DAILY_WERD).default as Int
                    ),
                    PID.FRIDAY_KAHF to sharedPrefs.getInt(
                        key = Preference.ExtraNotificationHour(PID.FRIDAY_KAHF).key,
                        defValue = Preference.ExtraNotificationHour(PID.FRIDAY_KAHF).default as Int
                    ) * 60 + sharedPrefs.getInt(
                        key = Preference.ExtraNotificationMinute(PID.FRIDAY_KAHF).key,
                        defValue = Preference.ExtraNotificationMinute(PID.FRIDAY_KAHF).default as Int
                    )
                ),
                lastNotificationsMillis = persistentMapOf<PID, Long>().mutate {
                    PID.entries.map {
                        it to 0L
                    }
                },
                notificationTypes = persistentMapOf<PID, NotificationType>().mutate {
                    PID.entries.map {
                        it to NotificationType.valueOf(
                            sharedPrefs.getString(
                                key = Preference.NotificationType(it).key,
                                defValue = Preference.NotificationType(it).default as String
                            )!!
                        )
                    }
                },
                notifyExtraNotifications = persistentMapOf(
                    PID.MORNING to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.MORNING).key,
                        defValue = Preference.NotifyExtraNotification(PID.MORNING).default as Boolean
                    ),
                    PID.EVENING to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.EVENING).key,
                        defValue = Preference.NotifyExtraNotification(PID.EVENING).default as Boolean
                    ),
                    PID.DAILY_WERD to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.DAILY_WERD).key,
                        defValue = Preference.NotifyExtraNotification(PID.DAILY_WERD).default as Boolean
                    ),
                    PID.FRIDAY_KAHF to sharedPrefs.getBoolean(
                        key = Preference.NotifyExtraNotification(PID.FRIDAY_KAHF).key,
                        defValue = Preference.NotifyExtraNotification(PID.FRIDAY_KAHF).default as Boolean
                    )
                ),
                prayerReminderOffsets = persistentMapOf(
                    PID.FAJR to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.FAJR).key,
                        defValue = Preference.ReminderOffset(PID.FAJR).default as Int
                    ),
                    PID.SUNRISE to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.SUNRISE).key,
                        defValue = Preference.ReminderOffset(PID.SUNRISE).default as Int
                    ),
                    PID.DHUHR to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.DHUHR).key,
                        defValue = Preference.ReminderOffset(PID.DHUHR).default as Int
                    ),
                    PID.ASR to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.ASR).key,
                        defValue = Preference.ReminderOffset(PID.ASR).default as Int
                    ),
                    PID.MAGHRIB to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.MAGHRIB).key,
                        defValue = Preference.ReminderOffset(PID.MAGHRIB).default as Int
                    ),
                    PID.ISHAA to sharedPrefs.getInt(
                        key = Preference.ReminderOffset(PID.ISHAA).key,
                        defValue = Preference.ReminderOffset(PID.ISHAA).default as Int
                    ),
                ),
            )
        }
    }

    fun getPrayersPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.PRAYERS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: PrayersPreferences ->
            currentData.copy(
                athanVoiceId = sharedPrefs.getString(
                    key = Preference.AthanVoice.key,
                    defValue = Preference.AthanVoice.default as String
                )!!.toInt(),
                calculationMethod = PTCalculationMethod.valueOf(
                    sharedPrefs.getString(
                        key = Preference.PrayerTimesCalculationMethod.key,
                        defValue = Preference.PrayerTimesCalculationMethod.default as String
                    )!!
                ),
                juristicMethod = PTJuristicMethod.valueOf(
                    sharedPrefs.getString(
                        key = Preference.PrayerTimesJuristicMethod.key,
                        defValue = Preference.PrayerTimesJuristicMethod.default as String
                    )!!
                ),
                highLatAdjustmentMethod = HighLatAdjustmentMethod.valueOf(
                    sharedPrefs.getString(
                        key = Preference.PrayerTimesAdjustment.key,
                        defValue = Preference.PrayerTimesAdjustment.default as String
                    )!!
                ),
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowPrayersTutorial.key,
                    defValue = Preference.ShowPrayersTutorial.default as Boolean
                ),
                timeOffsets = persistentMapOf(
                    PID.FAJR to sharedPrefs.getInt(
                        key = Preference.TimeOffset(PID.FAJR).key,
                        defValue = Preference.TimeOffset(PID.FAJR).default as Int
                    ),
                    PID.SUNRISE to sharedPrefs.getInt(
                        key = Preference.TimeOffset(PID.SUNRISE).key,
                        defValue = Preference.TimeOffset(PID.SUNRISE).default as Int
                    ),
                    PID.DHUHR to sharedPrefs.getInt(
                        key = Preference.TimeOffset(PID.DHUHR).key,
                        defValue = Preference.TimeOffset(PID.DHUHR).default as Int
                    ),
                    PID.ASR to sharedPrefs.getInt(
                        key = Preference.TimeOffset(PID.ASR).key,
                        defValue = Preference.TimeOffset(PID.ASR).default as Int
                    ),
                    PID.MAGHRIB to sharedPrefs.getInt(
                        key = Preference.TimeOffset(PID.MAGHRIB).key,
                        defValue = Preference.TimeOffset(PID.MAGHRIB).default as Int
                    ),
                    PID.ISHAA to sharedPrefs.getInt(
                        key = Preference.TimeOffset(PID.ISHAA).key,
                        defValue = Preference.TimeOffset(PID.ISHAA).default as Int
                    ),
                ),
            )
        }
    }

    fun getQuranPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.QURAN_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: QuranPreferences ->
            currentData.copy(
                ayaReciterId = sharedPrefs.getString(
                    key = Preference.AyaReciter.key,
                    defValue = Preference.AyaReciter.default as String
                )!!.toInt(),
                ayaRepeat = AyaRepeat.NONE,
                bookmarkedPage = sharedPrefs.getInt(
                    key = Preference.BookmarkedPage.key,
                    defValue = Preference.BookmarkedPage.default as Int
                ),
                bookmarkedSura = sharedPrefs.getInt(
                    key = Preference.BookmarkedSura.key,
                    defValue = Preference.BookmarkedSura.default as Int
                ),
                suraFavorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteSuar.key,
                            defValue = Preference.FavoriteSuar.default as String
                        )!!,
                        IntArray::class.java
                    ).mapIndexed { index, fav -> index to fav }
                },
                searcherMaxMatches = 10,
                textSize = sharedPrefs.getFloat(
                    key = Preference.QuranTextSize.key,
                    defValue = Preference.QuranTextSize.default as Float
                ),
                viewType = QuranViewType.valueOf(
                    sharedPrefs.getString(
                        key = Preference.QuranViewType.key,
                        defValue = Preference.QuranViewType.default as String
                    )!!
                ),
                shouldStopOnSuraEnd = sharedPrefs.getBoolean(
                    key = Preference.StopOnSuraEnd.key,
                    defValue = Preference.StopOnSuraEnd.default as Boolean
                ),
                shouldStopOnPageEnd = sharedPrefs.getBoolean(
                    key = Preference.StopOnPageEnd.key,
                    defValue = Preference.StopOnPageEnd.default as Boolean
                ),
                shouldShowMenuTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowQuranTutorial.key,
                    defValue = Preference.ShowQuranTutorial.default as Boolean
                ),
                shouldShowReaderTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowQuranViewerTutorial.key,
                    defValue = Preference.ShowQuranViewerTutorial.default as Boolean
                ),
                werdPage = sharedPrefs.getInt(
                    key = Preference.WerdPage.key,
                    defValue = Preference.WerdPage.default as Int
                ),
                isWerdDone = sharedPrefs.getBoolean(
                    key = Preference.WerdDone.key,
                    defValue = Preference.WerdDone.default as Boolean
                ),
            )
        }
    }

    fun getRecitationsPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.RECITATIONS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: RecitationsPreferences ->
            currentData.copy(
                reciterFavorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteReciters.key,
                            defValue = Preference.FavoriteReciters.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to (fav as Double).toInt()
                    }
                },
                lastPlayedMediaId = sharedPrefs.getString(
                    key = Preference.LastPlayedMediaId.key,
                    defValue = Preference.LastPlayedMediaId.default as String
                )!!,
                selectedNarrations = persistentMapOf<Int, Boolean>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.SelectedRewayat.key,
                            defValue = Preference.SelectedRewayat.default as String
                        )!!,
                        BooleanArray::class.java
                    ).mapIndexed { index, selected ->
                        index to selected
                    }
                },
                repeatMode = sharedPrefs.getInt(
                    key = Preference.TelawatRepeatMode.key,
                    defValue = Preference.TelawatRepeatMode.default as Int
                ),
                shuffleMode = sharedPrefs.getInt(
                    key = Preference.TelawatShuffleMode.key,
                    defValue = Preference.TelawatShuffleMode.default as Int
                ),
                lastProgress = sharedPrefs.getInt(
                    key = Preference.LastTelawaProgress.key,
                    defValue = Preference.LastTelawaProgress.default as Int
                ).toLong(),
            )
        }
    }

    fun getSupplicationsPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.SUPPLICATIONS_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: SupplicationsPreferences ->
            currentData.copy(
                textSize = sharedPrefs.getFloat(
                    key = Preference.AthkarTextSize.key,
                    defValue = Preference.AthkarTextSize.default as Float
                ),
                favorites = persistentMapOf<Int, Int>().mutate {
                    Gson().fromJson(
                        sharedPrefs.getString(
                            key = Preference.FavoriteAthkar.key,
                            defValue = Preference.FavoriteAthkar.default as String
                        )!!,
                        Array<Any>::class.java
                    ).mapIndexed { index, fav ->
                        index to (fav as Double).toInt()
                    }
                },
            )
        }
    }

    fun getUserPreferencesMigration(context: Context) {
        androidx.datastore.migrations.SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.USER_PREFERENCES_NAME,
        ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
            val storedLocation = sharedPrefs.getString(
                key = Preference.StoredLocation.key,
                defValue = Preference.StoredLocation.default as String
            )!!

            currentData.copy(
                location =
                    if (storedLocation == "{}") null
                    else {
                        val locationType = LocationType.valueOf(
                            sharedPrefs.getString(
                                key = Preference.LocationType.key,
                                defValue = Preference.LocationType.default as String
                            )!!
                        )
                        val myLocation = Gson().fromJson(storedLocation, MyLocation::class.java)

                        when (locationType) {
                            LocationType.Auto -> {
                                Location.FetchedLocation(
                                    latitude = myLocation.latitude,
                                    longitude = myLocation.longitude
                                )
                            }
                            LocationType.Manual -> {
                                Location.SelectedLocation(
                                    countryId = sharedPrefs.getInt(
                                        key = Preference.CountryID.key,
                                        defValue = Preference.CountryID.default as Int
                                    ),
                                    cityId = sharedPrefs.getInt(
                                        key = Preference.CityID.key,
                                        defValue = Preference.CityID.default as Int
                                    ),
                                    latitude = myLocation.latitude,
                                    longitude = myLocation.longitude
                                )
                            }
                            LocationType.None -> null
                        }
                    },
                quranPagesRecord = sharedPrefs.getInt(
                    key = Preference.QuranPagesRecord.key,
                    defValue = Preference.QuranPagesRecord.default as Int
                ),
                recitationsTimeRecord = sharedPrefs.getLong(
                    key = Preference.TelawatPlaybackRecord.key,
                    defValue = Preference.TelawatPlaybackRecord.default as Long
                ),
            )
        }
    }

}