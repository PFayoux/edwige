package com.pfayoux.edwige.domain

import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class Outcome { FOUND, MISSED }

/** Où en est la joueuse sur une carte. Enregistré après chaque tentative. */
@Serializable
data class CardProgress(
    /** Réponses fausses en champ libre (0 à 3). */
    val wrongFreeText: Int = 0,
    /** Ordre des 4 propositions du QCM, tiré une seule fois. Vide tant que le QCM n'a pas commencé. */
    val qcmOrder: List<String> = emptyList(),
    /** Propositions du QCM déjà choisies à tort (grisées). */
    val qcmWrong: List<String> = emptyList(),
    val outcome: Outcome? = null,
    val stars: Int = 0,
    val ticketUsed: Boolean = false,
) {
    val attemptsUsed: Int get() = wrongFreeText + qcmWrong.size
    val attemptsRemaining: Int get() = CardRules.MAX_ATTEMPTS - attemptsUsed
    val isDone: Boolean get() = outcome != null
    val isFound: Boolean get() = outcome == Outcome.FOUND
    val isMissed: Boolean get() = outcome == Outcome.MISSED
    val isInQcm: Boolean get() = !isDone && wrongFreeText >= CardRules.FREE_TEXT_ATTEMPTS
    val isStarted: Boolean get() = isDone || attemptsUsed > 0
}

object CardRules {
    const val MAX_ATTEMPTS = 5
    const val FREE_TEXT_ATTEMPTS = 3

    /** Applique le verdict d'une réponse en champ libre. Presque / trop vague ne coûtent rien. */
    fun applyFreeText(progress: CardProgress, verdict: Verdict, card: Card, random: Random = Random): CardProgress {
        check(!progress.isDone && !progress.isInQcm)
        return when (verdict) {
            Verdict.CORRECT -> progress.copy(
                outcome = Outcome.FOUND,
                stars = if (progress.wrongFreeText == 0) 3 else 2,
            )
            Verdict.PRESQUE, Verdict.TROP_VAGUE -> progress
            Verdict.FAUX -> {
                val wrong = progress.wrongFreeText + 1
                if (wrong >= FREE_TEXT_ATTEMPTS) {
                    progress.copy(wrongFreeText = wrong, qcmOrder = card.qcmOptions.shuffled(random))
                } else {
                    progress.copy(wrongFreeText = wrong)
                }
            }
        }
    }

    /** Applique un choix dans le QCM. */
    fun applyQcm(progress: CardProgress, choice: String, card: Card): CardProgress {
        check(progress.isInQcm && choice !in progress.qcmWrong)
        if (choice == card.qcmAnswer) return progress.copy(outcome = Outcome.FOUND, stars = 1)
        val wrong = progress.qcmWrong + choice
        return if (progress.wrongFreeText + wrong.size >= MAX_ATTEMPTS) {
            progress.copy(qcmWrong = wrong, outcome = Outcome.MISSED, stars = 0)
        } else {
            progress.copy(qcmWrong = wrong)
        }
    }

    /**
     * Mode organisateur : valider la carte comme si la dernière réponse refusée avait été juste.
     */
    fun validateManually(progress: CardProgress): CardProgress {
        if (progress.isFound) return progress
        val stars = when {
            progress.qcmWrong.isNotEmpty() -> 1
            progress.wrongFreeText <= 1 -> 3
            else -> 2
        }
        return progress.copy(outcome = Outcome.FOUND, stars = stars)
    }

    /** Mode organisateur : rendre une tentative (annule aussi une carte manquée). */
    fun giveBackAttempt(progress: CardProgress): CardProgress {
        if (progress.isFound) return progress
        return when {
            progress.qcmWrong.isNotEmpty() ->
                progress.copy(qcmWrong = progress.qcmWrong.dropLast(1), outcome = null, stars = 0)
            progress.wrongFreeText > 0 -> {
                val wrong = progress.wrongFreeText - 1
                progress.copy(
                    wrongFreeText = wrong,
                    qcmOrder = if (wrong < FREE_TEXT_ATTEMPTS) emptyList() else progress.qcmOrder,
                    outcome = null,
                    stars = 0,
                )
            }
            else -> progress
        }
    }
}
