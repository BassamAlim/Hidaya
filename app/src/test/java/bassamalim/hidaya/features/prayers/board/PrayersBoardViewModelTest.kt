package bassamalim.hidaya.features.prayers.board

import android.app.Application
import app.cash.turbine.test
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrayersBoardViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val app = mockk<Application>(relaxed = true)
    private val domain = mockk<PrayersBoardDomain>(relaxed = true)
    private val navigator = mockk<Navigator>(relaxed = true)

    private fun buildViewModel(): PrayersBoardViewModel {
        every { domain.getLocation() } returns flowOf(null)
        every { domain.getPrayerSettings() } returns flowOf(emptyMap())
        every { domain.getPrayerTimesCalculatorSettings() } returns
            flowOf(PrayerTimeCalculatorSettings())
        every { domain.getPrayerNames() } returns emptyMap()
        every { domain.getLanguage() } returns Language.ARABIC
        every { domain.getHijriMonths() } returns Array(12) { "" }
        return PrayersBoardViewModel(app, domain, navigator)
    }

    @Test
    fun `initial state has loading true, becomes false after initializeData`() = runTest {
        val vm = buildViewModel()

        vm.uiState.test {
            val initial = awaitItem()
            assertTrue("expected loading=true on first emit", initial.loading)

            advanceUntilIdle()

            val updated = awaitItem()
            assertFalse("expected loading=false after init", updated.loading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onReportTogglePrayer adds prayer then removes it on second call`() = runTest {
        val vm = buildViewModel()

        vm.uiState.test {
            // consume init emissions
            awaitItem()
            advanceUntilIdle()
            awaitItem() // loading=false emit

            vm.onReportTogglePrayer(Prayer.FAJR)
            val afterAdd = awaitItem()
            assertTrue(Prayer.FAJR in afterAdd.report.wrongPrayers)

            vm.onReportTogglePrayer(Prayer.FAJR)
            val afterRemove = awaitItem()
            assertFalse(Prayer.FAJR in afterRemove.report.wrongPrayers)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCorrectTimePickerConfirm formats time as 02d colon 02d and clears target`() = runTest {
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()
            advanceUntilIdle()
            awaitItem()

            // open picker for DHUHR
            vm.onCorrectTimePickerOpen(Prayer.DHUHR)
            awaitItem() // timePickerTarget = DHUHR

            vm.onCorrectTimePickerConfirm(hour = 9, minute = 5)
            val state = awaitItem()

            assertEquals("09:05", state.report.correctTimes[Prayer.DHUHR])
            assertNull("timePickerTarget should be null after confirm", state.report.timePickerTarget)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onReportDismiss sets dialogShown to false`() = runTest {
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()
            advanceUntilIdle()
            awaitItem()

            // Force dialog open by updating _uiState directly via the public helper
            vm.onReportDismiss()
            val state = awaitItem()

            assertFalse(state.report.dialogShown)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onReportNext from CHECKS goes to FORM, from FORM stays at FORM`() = runTest {
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()
            advanceUntilIdle()
            awaitItem()

            // Default step is CHECKS — advance to FORM
            vm.onReportNext()
            val atForm = awaitItem()
            assertEquals(ReportStep.FORM, atForm.report.step)

            // Calling Next again from FORM should stay at FORM
            vm.onReportNext()
            val stillForm = awaitItem()
            assertEquals(ReportStep.FORM, stillForm.report.step)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
