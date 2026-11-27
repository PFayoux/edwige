package com.pfayoux.edwige

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pfayoux.edwige.data.SaveStore
import com.pfayoux.edwige.data.SaveData
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.domain.AnswerMatcher
import com.pfayoux.edwige.domain.CalendarContent
import com.pfayoux.edwige.domain.CardProgress
import com.pfayoux.edwige.domain.CardRules
import com.pfayoux.edwige.domain.CardStatus
import com.pfayoux.edwige.domain.Verdict
import com.pfayoux.edwige.notifications.ReminderScheduler
import java.time.LocalDate
import kotlin.random.Random

sealed interface Screen {
    data object Onboarding : Screen
    data object Home : Screen
    data class Answer(val day: Int) : Screen
    data class Reveal(val day: Int, val celebrate: Boolean) : Screen
    data object Tickets : Screen
    data object Journey : Screen
    data object Organizer : Screen
}

/** Quel message de réaction afficher sous la réponse (textes dans strings.xml). */
enum class FeedbackMessage { ALMOST, TOO_VAGUE, WRONG, WRONG_THEN_QCM, WRONG_LAST_CHANCE }

/**
 * Retour affiché sous la réponse. [pick] choisit le message dans la liste correspondante,
 * fixé au moment de l'essai pour ne pas changer à chaque affichage.
 * [id] change à chaque essai pour rejouer l'animation.
 */
data class Feedback(val day: Int, val verdict: Verdict, val message: FeedbackMessage, val pick: Int, val id: Long)

class GameViewModel(
    val content: CalendarContent,
    private val store: SaveStore,
    private val reminders: ReminderScheduler,
    private val clock: () -> LocalDate = LocalDate::now,
    private val random: Random = Random,
) : ViewModel() {

    private val matcher = AnswerMatcher.forContent(content)

    var save by mutableStateOf(store.load())
        private set

    private var realToday by mutableStateOf(clock())

    val today: LocalDate get() = save.today(realToday)
    val isDateSimulated: Boolean get() = save.simulatedDate != null

    val stack = mutableStateListOf(if (save.onboardingDone) Screen.Home else Screen.Onboarding)
    val current: Screen get() = stack.last()

    var feedback by mutableStateOf<Feedback?>(null)
        private set

    // --- Lecture -----------------------------------------------------------

    fun progress(day: Int): CardProgress = save.progress(day)
    fun status(day: Int): CardStatus = AdventCalendar.status(day, save.cards[day], today)
    val foundCount: Int get() = save.cards.values.count { it.isFound }
    val totalStars: Int get() = save.cards.values.sumOf { it.stars }
    val allPlayed: Boolean get() = (1..AdventCalendar.DAYS).all { progress(it).isDone }
    val todayCard: Int? get() = AdventCalendar.dayOf(today)

    // --- Navigation --------------------------------------------------------

    fun navigate(screen: Screen) {
        feedback = null
        stack.add(screen)
    }

    private fun replaceTop(screen: Screen) {
        feedback = null
        stack[stack.lastIndex] = screen
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        feedback = null
        stack.removeAt(stack.lastIndex)
        return true
    }

    fun goHome() {
        feedback = null
        stack.clear()
        stack.add(Screen.Home)
    }

    fun openCard(day: Int) = when (status(day)) {
        CardStatus.AVAILABLE -> navigate(Screen.Answer(day))
        CardStatus.FOUND, CardStatus.MISSED -> navigate(Screen.Reveal(day, celebrate = false))
        CardStatus.LOCKED -> Unit
    }

    // --- Jeu ---------------------------------------------------------------

    fun submitFreeText(day: Int, input: String) {
        val card = content.card(day)
        val before = progress(day)
        if (before.isDone || before.isInQcm || input.isBlank()) return
        val verdict = matcher.evaluate(input, card)
        val after = CardRules.applyFreeText(before, verdict, card)
        update { it.withProgress(day, after) }
        if (after.isFound) {
            replaceTop(Screen.Reveal(day, celebrate = true))
            return
        }
        val message = when (verdict) {
            Verdict.PRESQUE -> FeedbackMessage.ALMOST
            Verdict.TROP_VAGUE -> FeedbackMessage.TOO_VAGUE
            else -> if (after.isInQcm) FeedbackMessage.WRONG_THEN_QCM else FeedbackMessage.WRONG
        }
        showFeedback(day, verdict, message)
    }

    fun chooseQcm(day: Int, option: String) {
        val card = content.card(day)
        val before = progress(day)
        if (!before.isInQcm || option in before.qcmWrong) return
        val after = CardRules.applyQcm(before, option, card)
        update { it.withProgress(day, after) }
        when {
            after.isDone -> replaceTop(Screen.Reveal(day, celebrate = after.isFound))
            else -> showFeedback(day, Verdict.FAUX, FeedbackMessage.WRONG_LAST_CHANCE)
        }
    }

    private fun showFeedback(day: Int, verdict: Verdict, message: FeedbackMessage) {
        feedback = Feedback(day, verdict, message, random.nextInt(Int.MAX_VALUE), System.nanoTime())
    }

    fun markTicketUsed(day: Int) = update {
        it.withProgress(day, it.progress(day).copy(ticketUsed = true))
    }

    // --- Réglages ----------------------------------------------------------

    fun finishOnboarding(notificationsEnabled: Boolean) {
        update { it.copy(onboardingDone = true, notificationsEnabled = notificationsEnabled) }
        reminders.reschedule()
        replaceTop(Screen.Home)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        update { it.copy(notificationsEnabled = enabled) }
        reminders.reschedule()
    }

    fun refreshDate() {
        realToday = clock()
    }

    // --- Mode organisateur -------------------------------------------------

    fun setSimulatedDate(date: LocalDate?) = update { it.copy(simulatedDate = date?.toString()) }
    fun shiftSimulatedDate(days: Long) = setSimulatedDate(today.plusDays(days))
    fun unlockAll() = setSimulatedDate(AdventCalendar.unlockDate(AdventCalendar.DAYS))

    fun validateManually(day: Int) = update {
        it.withProgress(day, CardRules.validateManually(it.progress(day)))
    }

    fun giveBackAttempt(day: Int) = update {
        it.withProgress(day, CardRules.giveBackAttempt(it.progress(day)))
    }

    fun testNotification() = reminders.showNow()

    fun resetEverything() {
        update { SaveData() }
        reminders.reschedule()
        feedback = null
        stack.clear()
        stack.add(Screen.Onboarding)
    }

    private fun update(transform: (SaveData) -> SaveData) {
        save = transform(save)
        store.save(save)
    }
}
