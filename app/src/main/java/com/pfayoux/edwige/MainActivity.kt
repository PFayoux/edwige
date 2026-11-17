package com.pfayoux.edwige

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pfayoux.edwige.data.ContentRepository
import com.pfayoux.edwige.data.ProgressStore
import com.pfayoux.edwige.notifications.AndroidReminderScheduler
import com.pfayoux.edwige.notifications.Reminders
import com.pfayoux.edwige.ui.screens.AnswerScreen
import com.pfayoux.edwige.ui.screens.HomeScreen
import com.pfayoux.edwige.ui.screens.JourneyScreen
import com.pfayoux.edwige.ui.screens.OnboardingScreen
import com.pfayoux.edwige.ui.screens.OrganizerScreen
import com.pfayoux.edwige.ui.screens.RevealScreen
import com.pfayoux.edwige.ui.screens.TicketsScreen
import com.pfayoux.edwige.ui.theme.EdwigeTheme
import com.pfayoux.edwige.ui.theme.Paper

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        viewModelFactory {
            initializer {
                GameViewModel(
                    content = ContentRepository.load(applicationContext),
                    store = ProgressStore(applicationContext),
                    reminders = AndroidReminderScheduler(applicationContext),
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Paper.toArgb(), Paper.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Paper.toArgb(), Paper.toArgb()),
        )
        super.onCreate(savedInstanceState)
        setContent {
            EdwigeTheme {
                Surface(color = Paper, modifier = Modifier.fillMaxSize()) { App(viewModel) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDate()
        Reminders.reschedule(this)
    }
}

private fun androidx.compose.ui.graphics.Color.toArgb(): Int =
    android.graphics.Color.argb((alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())

@Composable
fun App(vm: GameViewModel) {
    BackHandler(enabled = vm.stack.size > 1) { vm.back() }
    AnimatedContent(
        targetState = vm.current,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen",
    ) { screen ->
        when (screen) {
            Screen.Onboarding -> OnboardingScreen(vm)
            Screen.Home -> HomeScreen(vm)
            is Screen.Answer -> AnswerScreen(vm, screen.day)
            is Screen.Reveal -> RevealScreen(vm, screen.day, screen.celebrate)
            Screen.Tickets -> TicketsScreen(vm)
            Screen.Journey -> JourneyScreen(vm)
            Screen.Organizer -> OrganizerScreen(vm)
        }
    }
}
