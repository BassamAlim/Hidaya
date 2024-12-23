package bassamalim.hidaya.core.di

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.data.repositories.LiveContentRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.data.repositories.QuizRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.helpers.Alarm
import bassamalim.hidaya.features.about.domain.AboutDomain
import bassamalim.hidaya.features.books.bookChaptersMenu.domain.BookChaptersDomain
import bassamalim.hidaya.features.books.bookReader.domain.BookReaderDomain
import bassamalim.hidaya.features.books.bookSearcher.domain.BookSearcherDomain
import bassamalim.hidaya.features.books.booksMenu.domain.BooksMenuDomain
import bassamalim.hidaya.features.dateConverter.domain.DateConverterDomain
import bassamalim.hidaya.features.dateEditor.domain.DateEditorDomain
import bassamalim.hidaya.features.hijriDatePicker.domain.HijriDatePickerDomain
import bassamalim.hidaya.features.home.domain.HomeDomain
import bassamalim.hidaya.features.leaderboard.domain.LeaderboardDomain
import bassamalim.hidaya.features.locationPicker.domain.LocationPickerDomain
import bassamalim.hidaya.features.locator.domain.LocatorDomain
import bassamalim.hidaya.features.main.domain.MainDomain
import bassamalim.hidaya.features.onboarding.domain.OnboardingDomain
import bassamalim.hidaya.features.prayers.board.domain.PrayersBoardDomain
import bassamalim.hidaya.features.prayers.extraReminderSettings.domain.PrayerExtraReminderSettingsDomain
import bassamalim.hidaya.features.prayers.settings.domain.PrayerSettingsDomain
import bassamalim.hidaya.features.qibla.domain.QiblaDomain
import bassamalim.hidaya.features.quiz.result.domain.QuizResultDomain
import bassamalim.hidaya.features.quiz.test.domain.QuizTestDomain
import bassamalim.hidaya.features.quran.reader.domain.QuranReaderDomain
import bassamalim.hidaya.features.quran.settings.domain.QuranSettingsDomain
import bassamalim.hidaya.features.quran.surasMenu.domain.QuranSurasDomain
import bassamalim.hidaya.features.radio.domain.RadioDomain
import bassamalim.hidaya.features.recitations.player.domain.RecitationPlayerDomain
import bassamalim.hidaya.features.recitations.recitersMenu.domain.RecitationRecitersMenuDomain
import bassamalim.hidaya.features.recitations.surasMenu.domain.RecitationSurasMenuDomain
import bassamalim.hidaya.features.remembrances.reader.domain.RemembranceReaderDomain
import bassamalim.hidaya.features.remembrances.remembrancesMenu.domain.RemembrancesMenuDomain
import bassamalim.hidaya.features.settings.domain.SettingsDomain
import bassamalim.hidaya.features.tv.domain.TvDomain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides @Singleton
    fun provideAboutDomain(
        appStateRepository: AppStateRepository,
        booksRepository: BooksRepository,
        prayersRepository: PrayersRepository,
        quranRepository: QuranRepository
    ) = AboutDomain(appStateRepository, booksRepository, prayersRepository, quranRepository)

    @Provides @Singleton
    fun provideBookChaptersDomain(
        booksRepository: BooksRepository,
        appSettingsRepository: AppSettingsRepository
    ) = BookChaptersDomain(booksRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideBookReaderDomain(
        booksRepository: BooksRepository,
        appSettingsRepository: AppSettingsRepository
    ) = BookReaderDomain(booksRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideBookSearcherDomain(
        booksRepository: BooksRepository,
        appSettingsRepository: AppSettingsRepository
    ) = BookSearcherDomain(booksRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideBooksMenuDomain(
        booksRepository: BooksRepository,
        appSettingsRepository: AppSettingsRepository
    ) = BooksMenuDomain(booksRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideDateConverterDomain(
        appSettingsRepository: AppSettingsRepository,
        appStateRepository: AppStateRepository
    ) = DateConverterDomain(appSettingsRepository, appStateRepository)

    @Provides @Singleton
    fun provideDateEditorDomain(
        appSettingsRepository: AppSettingsRepository
    ) = DateEditorDomain(appSettingsRepository)

    @Provides @Singleton
    fun provideHijriDatePickerDomain(
        appSettingsRepository: AppSettingsRepository,
        appStateRepository: AppStateRepository
    ) = HijriDatePickerDomain(appSettingsRepository, appStateRepository)

    @Provides @Singleton
    fun provideHomeDomain(
        app: Application,
        prayersRepository: PrayersRepository,
        locationRepository: LocationRepository,
        quranRepository: QuranRepository,
        userRepository: UserRepository,
        appSettingsRepository: AppSettingsRepository
    ) = HomeDomain(
        app,
        prayersRepository,
        locationRepository,
        quranRepository,
        userRepository,
        appSettingsRepository
    )

    @Provides @Singleton
    fun provideLeaderboardDomain(
        app: Application,
        userRepository: UserRepository,
        appSettingsRepository: AppSettingsRepository
    ) = LeaderboardDomain(app, userRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideLocationPickerDomain(
        locationRepository: LocationRepository,
        appSettingsRepository: AppSettingsRepository
    ) = LocationPickerDomain(locationRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideLocatorDomain(
        app: Application,
        locationRepository: LocationRepository,
        appSettingsRepository: AppSettingsRepository
    ) = LocatorDomain(app, locationRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideMainDomain(
        appStateRepository: AppStateRepository,
        appSettingsRepository: AppSettingsRepository
    ) = MainDomain(appStateRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideOnboardingDomain(
        appStateRepository: AppStateRepository,
        appSettingsRepository: AppSettingsRepository
    ) = OnboardingDomain(appStateRepository, appSettingsRepository)

    @Provides @Singleton
    fun providePrayerReminderDomain(
        prayersRepository: PrayersRepository,
        notificationsRepository: NotificationsRepository,
        appSettingsRepository: AppSettingsRepository,
        alarm: Alarm
    ) = PrayerExtraReminderSettingsDomain(
        prayersRepository,
        notificationsRepository,
        appSettingsRepository,
        alarm
    )

    @Provides @Singleton
    fun providePrayersBoardDomain(
        prayersRepository: PrayersRepository,
        locationRepository: LocationRepository,
        notificationsRepository: NotificationsRepository,
        appStateRepository: AppStateRepository,
        appSettingsRepository: AppSettingsRepository,
    ) = PrayersBoardDomain(
        prayersRepository,
        locationRepository,
        notificationsRepository,
        appStateRepository,
        appSettingsRepository,
    )

    @Provides @Singleton
    fun providePrayerSettingsDomain(
        prayersRepository: PrayersRepository,
        notificationsRepository: NotificationsRepository,
        appSettingsRepository: AppSettingsRepository
    ) = PrayerSettingsDomain(prayersRepository, notificationsRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideQiblaDomain(
        app: Application,
        locationRepository: LocationRepository,
        appSettingsRepository: AppSettingsRepository
    ) = QiblaDomain(app, locationRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideQuizResultDomain(
        appSettingsRepository: AppSettingsRepository
    ) = QuizResultDomain(appSettingsRepository)

    @Provides @Singleton
    fun provideQuizTestDomain(
        quizRepository: QuizRepository,
        appSettingsRepository: AppSettingsRepository
    ) = QuizTestDomain(quizRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideQuranMenu(
        quranRepository: QuranRepository,
        appSettingsRepository: AppSettingsRepository
    ) = QuranSurasDomain(quranRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideQuranReaderDomain(
        app: Application,
        quranRepository: QuranRepository,
        appSettingsRepository: AppSettingsRepository,
        userRepository: UserRepository
    ) = QuranReaderDomain(app, quranRepository, appSettingsRepository, userRepository)

    @Provides @Singleton
    fun provideQuranSettings(
        quranRepository: QuranRepository,
        recitationsRepository: RecitationsRepository,
        appSettingsRepository: AppSettingsRepository
    ) = QuranSettingsDomain(quranRepository, recitationsRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideRadioDomain(
        liveContentRepository: LiveContentRepository
    ) = RadioDomain(liveContentRepository)

    @Provides @Singleton
    fun provideRecitationPlayerDomain(
        app: Application,
        recitationsRepository: RecitationsRepository,
        quranRepository: QuranRepository,
        appSettingsRepository: AppSettingsRepository
    ) = RecitationPlayerDomain(app, recitationsRepository, quranRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideRecitationRecitersMenuDomain(
        app: Application,
        recitationsRepository: RecitationsRepository,
        quranRepository: QuranRepository,
        appSettingsRepository: AppSettingsRepository
    ) = RecitationRecitersMenuDomain(
        app,
        recitationsRepository,
        quranRepository,
        appSettingsRepository
    )

    @Provides @Singleton
    fun provideRecitationSurasMenuDomain(
        app: Application,
        recitationsRepository: RecitationsRepository,
        quranRepository: QuranRepository,
        appSettingsRepository: AppSettingsRepository
    ) = RecitationSurasMenuDomain(
        app,
        recitationsRepository,
        quranRepository,
        appSettingsRepository
    )

    @Provides @Singleton
    fun provideRemembranceReaderDomain(
        remembrancesRepository: RemembrancesRepository,
        appSettingsRepository: AppSettingsRepository
    ) = RemembranceReaderDomain(remembrancesRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideRemembrancesMenuDomain(
        remembrancesRepository: RemembrancesRepository,
        appSettingsRepository: AppSettingsRepository
    ) = RemembrancesMenuDomain(remembrancesRepository, appSettingsRepository)

    @Provides @Singleton
    fun provideSettingsDomain(
        appSettingsRepository: AppSettingsRepository,
        prayersRepository: PrayersRepository,
        notificationsRepository: NotificationsRepository,
        locationRepository: LocationRepository,
        alarm: Alarm
    ) = SettingsDomain(
        appSettingsRepository,
        prayersRepository,
        notificationsRepository,
        locationRepository,
        alarm
    )

    @Provides @Singleton
    fun provideTvDomain(
        liveContentRepository: LiveContentRepository,
        appSettingsRepository: AppSettingsRepository
    ) = TvDomain(liveContentRepository, appSettingsRepository)

}