package com.pfayoux.edwige.ui

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.app.ActivityOptionsCompat
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.data.SaveData
import com.pfayoux.edwige.data.SaveStore
import com.pfayoux.edwige.domain.CalendarContent
import com.pfayoux.edwige.domain.CardProgress
import com.pfayoux.edwige.domain.Outcome
import com.pfayoux.edwige.notifications.ReminderScheduler
import com.pfayoux.edwige.ui.screens.AnswerScreen
import com.pfayoux.edwige.ui.screens.HomeScreen
import com.pfayoux.edwige.ui.screens.JourneyScreen
import com.pfayoux.edwige.ui.screens.OnboardingScreen
import com.pfayoux.edwige.ui.screens.OrganizerScreen
import com.pfayoux.edwige.ui.screens.RevealScreen
import com.pfayoux.edwige.ui.screens.TicketsScreen
import com.pfayoux.edwige.ui.theme.EdwigeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.time.LocalDate

/**
 * Captures de tous les écrans sur plusieurs tailles de téléphone et de police.
 * `./gradlew recordPaparazziDebug` puis regarder app/src/test/snapshots/images.
 */
@RunWith(Parameterized::class)
class ScreensSnapshotTest(private val name: String, config: DeviceConfig) {

    companion object {
        /** Petit téléphone 360 dp, Pixel, grand Samsung Galaxy (412 dp, haute densité). */
        private val SMALL = DeviceConfig.NEXUS_5
        private val PIXEL = DeviceConfig.PIXEL_5
        private val GALAXY = DeviceConfig(screenHeight = 3120, screenWidth = 1440, xdpi = 505, ydpi = 505, density = Density.create(560), size = com.android.resources.ScreenSize.NORMAL, ratio = com.android.resources.ScreenRatio.LONG)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun configs() = listOf(
            arrayOf("small", SMALL),
            arrayOf("pixel", PIXEL),
            arrayOf("galaxy", GALAXY),
            arrayOf("pixel_font150", PIXEL.copy(fontScale = 1.5f)),
            arrayOf("small_font200", SMALL.copy(fontScale = 2f)),
        )
    }

    @get:Rule val paparazzi = Paparazzi(deviceConfig = config, maxPercentDifference = 0.1)

    private val content = CalendarContent.parse(File("src/main/assets/calendrier_avent.json").readText())

    private class MemoryStore(var data: SaveData) : SaveStore {
        override fun load() = data
        override fun save(data: SaveData) { this.data = data }
    }

    private object NoReminders : ReminderScheduler {
        override fun reschedule() {}
        override fun showNow() {}
    }

    private val midGame = SaveData(
        onboardingDone = true,
        cards = mapOf(
            1 to CardProgress(outcome = Outcome.FOUND, stars = 3, ticketUsed = true),
            2 to CardProgress(wrongFreeText = 1, outcome = Outcome.FOUND, stars = 2),
            3 to CardProgress(wrongFreeText = 3, qcmWrong = listOf("Alicante", "Guadalest"), outcome = Outcome.MISSED),
            4 to CardProgress(wrongFreeText = 3, outcome = Outcome.FOUND, stars = 1),
            5 to CardProgress(outcome = Outcome.FOUND, stars = 3),
            7 to CardProgress(wrongFreeText = 3, qcmOrder = listOf("Belvédère", "Kunsthistorisches Museum", "Schönbrunn", "Musée des Arts forains"), qcmWrong = listOf("Belvédère")),
            9 to CardProgress(wrongFreeText = 1),
            14 to CardProgress(outcome = Outcome.FOUND, stars = 2),
        ),
    )

    private fun vm(save: SaveData = midGame, today: LocalDate = LocalDate.of(2026, 12, 14)) =
        GameViewModel(content, MemoryStore(save), NoReminders) { today }

    private fun snap(content: @Composable () -> Unit) = paparazzi.snapshot {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides FakeRegistryOwner) {
            EdwigeTheme { content() }
        }
    }

    @Test fun home() = snap { HomeScreen(vm()) }

    @Test fun homeBeforeDecember() = snap { HomeScreen(vm(SaveData(onboardingDone = true), LocalDate.of(2026, 11, 28))) }

    @Test fun answerFreeTextWithFeedback() {
        val vm = vm()
        vm.submitFreeText(12, "Tour")
        snap { AnswerScreen(vm, 12) }
    }

    @Test fun answerQcm() = snap { AnswerScreen(vm(), 7) }

    @Test fun revealFound() = snap { RevealScreen(vm(), 14, celebrate = false) }

    @Test fun revealMissed() = snap { RevealScreen(vm(), 3, celebrate = false) }

    @Test fun tickets() = snap { TicketsScreen(vm()) }

    @Test fun onboarding() = snap { OnboardingScreen(vm(SaveData())) }

    @Test fun journey() {
        val all = SaveData(onboardingDone = true, cards = (1..24).associateWith {
            if (it % 5 == 0) CardProgress(outcome = Outcome.MISSED) else CardProgress(outcome = Outcome.FOUND, stars = it % 3 + 1)
        })
        snap { JourneyScreen(vm(all, LocalDate.of(2026, 12, 25))) }
    }

    @Test fun organizer() = snap { OrganizerScreen(vm()) }
}

private object FakeRegistryOwner : ActivityResultRegistryOwner {
    override val activityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?) {}
    }
}
