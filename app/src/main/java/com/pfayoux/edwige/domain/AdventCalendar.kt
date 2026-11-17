package com.pfayoux.edwige.domain

import java.time.LocalDate

enum class CardStatus { LOCKED, AVAILABLE, FOUND, MISSED }

object AdventCalendar {
    const val YEAR = 2026
    const val DAYS = 24

    fun unlockDate(day: Int): LocalDate = LocalDate.of(YEAR, 12, day)

    /** La carte N s'ouvre le N décembre à minuit (heure du téléphone) et reste ouverte ensuite. */
    fun isUnlocked(day: Int, today: LocalDate): Boolean = !today.isBefore(unlockDate(day))

    /** Le numéro de la carte du jour, ou null hors du 1er-24 décembre. */
    fun dayOf(today: LocalDate): Int? =
        if (today.year == YEAR && today.monthValue == 12 && today.dayOfMonth <= DAYS) today.dayOfMonth else null

    fun status(day: Int, progress: CardProgress?, today: LocalDate): CardStatus = when {
        progress?.isFound == true -> CardStatus.FOUND
        progress?.isMissed == true -> CardStatus.MISSED
        isUnlocked(day, today) -> CardStatus.AVAILABLE
        else -> CardStatus.LOCKED
    }
}
