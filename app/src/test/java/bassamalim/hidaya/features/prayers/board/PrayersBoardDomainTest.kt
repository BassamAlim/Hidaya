package bassamalim.hidaya.features.prayers.board

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayerTimesReport
import bassamalim.hidaya.core.data.repositories.PrayerTimesReportRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrayersBoardDomainTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val prayersRepository = mockk<PrayersRepository>(relaxed = true)
    private val locationRepository = mockk<LocationRepository>(relaxed = true)
    private val notificationsRepository = mockk<NotificationsRepository>(relaxed = true)
    private val appStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val appSettingsRepository = mockk<AppSettingsRepository>(relaxed = true)
    private val reportRepository = mockk<PrayerTimesReportRepository>(relaxed = true)

    private val domain by lazy {
        PrayersBoardDomain(
            prayersRepository,
            locationRepository,
            notificationsRepository,
            appStateRepository,
            appSettingsRepository,
            reportRepository
        )
    }

    @Test
    fun `getPrayerSettings combines notification types and reminder offsets`() = runTest {
        val notificationTypes = mapOf(
            Reminder.Prayer.Fajr    to NotificationType.ATHAN,
            Reminder.Prayer.Sunrise to NotificationType.NOTIFICATION,
            Reminder.Prayer.Dhuhr   to NotificationType.NOTIFICATION,
            Reminder.Prayer.Asr     to NotificationType.NOTIFICATION,
            Reminder.Prayer.Maghrib to NotificationType.ATHAN,
            Reminder.Prayer.Ishaa   to NotificationType.SILENT
        )
        val reminderOffsets = mapOf(
            Reminder.PrayerExtra.Fajr    to 10,
            Reminder.PrayerExtra.Sunrise to 0,
            Reminder.PrayerExtra.Dhuhr   to 5,
            Reminder.PrayerExtra.Asr     to 0,
            Reminder.PrayerExtra.Maghrib to 15,
            Reminder.PrayerExtra.Ishaa   to 0
        )
        every { notificationsRepository.getNotificationTypes() } returns flowOf(notificationTypes)
        every { notificationsRepository.getPrayerExtraReminderTimeOffsets() } returns flowOf(reminderOffsets)

        val result = domain.getPrayerSettings().first()

        val expectedPrayers = listOf(
            Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHAA
        )
        assertEquals(expectedPrayers.toSet(), result.keys)
        assertEquals(NotificationType.ATHAN, result[Prayer.FAJR]!!.notificationType)
        assertEquals(10, result[Prayer.FAJR]!!.reminderOffset)
        assertEquals(NotificationType.ATHAN, result[Prayer.MAGHRIB]!!.notificationType)
        assertEquals(15, result[Prayer.MAGHRIB]!!.reminderOffset)
        assertEquals(NotificationType.SILENT, result[Prayer.ISHAA]!!.notificationType)
    }

    @Test
    fun `submitReport delegates to repository and returns its result`() = runTest {
        val report = PrayerTimesReport(
            language = Language.ENGLISH,
            location = null,
            locationName = "Mecca",
            calculatorSettings = PrayerTimeCalculatorSettings(),
            computedTimes = mapOf(Prayer.FAJR to "04:55"),
            wrongPrayers = setOf(Prayer.FAJR),
            correctTimes = mapOf(Prayer.FAJR to "05:00"),
            notes = ""
        )
        coEvery { reportRepository.submitReport(report) } returns true

        val result = domain.submitReport(report)

        assertTrue(result)
        coVerify(exactly = 1) { reportRepository.submitReport(report) }
    }

    @Test
    fun `getCountryName delegates to LocationRepository with correct args`() = runTest {
        val countryId = 42
        val language = Language.ENGLISH
        coEvery {
            locationRepository.getCountryName(countryId = countryId, language = language)
        } returns "Saudi Arabia"

        val result = domain.getCountryName(countryId = countryId, language = language)

        assertEquals("Saudi Arabia", result)
        coVerify(exactly = 1) {
            locationRepository.getCountryName(countryId = countryId, language = language)
        }
    }

    @Test
    fun `getCityName delegates to LocationRepository with correct args`() = runTest {
        val cityId = 7
        val language = Language.ARABIC
        coEvery {
            locationRepository.getCityName(cityId = cityId, language = language)
        } returns "مكة"

        val result = domain.getCityName(cityId = cityId, language = language)

        assertEquals("مكة", result)
        coVerify(exactly = 1) {
            locationRepository.getCityName(cityId = cityId, language = language)
        }
    }
}
