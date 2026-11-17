package com.pfayoux.edwige.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class CardRulesTest {

    private val card = Card(
        jour = 15, lieu = "Plage de Porquerolles", acceptedAnswers = listOf("porquerolles"),
        qcmAnswer = "Porquerolles", qcmWrongAnswers = listOf("Giens", "Saint-Tropez", "Split"),
        texte = "…", cadeau = "Spa",
    )

    private fun wrongTimes(n: Int): CardProgress =
        (1..n).fold(CardProgress()) { p, _ -> CardRules.applyFreeText(p, Verdict.FAUX, card, Random(1)) }

    @Test fun `first try gives three stars`() {
        val p = CardRules.applyFreeText(CardProgress(), Verdict.CORRECT, card)
        assertEquals(Outcome.FOUND, p.outcome); assertEquals(3, p.stars)
    }

    @Test fun `second or third try gives two stars`() {
        assertEquals(2, CardRules.applyFreeText(wrongTimes(1), Verdict.CORRECT, card).stars)
        assertEquals(2, CardRules.applyFreeText(wrongTimes(2), Verdict.CORRECT, card).stars)
    }

    @Test fun `almost and too vague do not cost an attempt`() {
        val p = CardRules.applyFreeText(CardProgress(), Verdict.PRESQUE, card)
        val q = CardRules.applyFreeText(p, Verdict.TROP_VAGUE, card)
        assertEquals(5, q.attemptsRemaining)
    }

    @Test fun `third wrong answer switches to qcm with shuffled options`() {
        assertFalse(wrongTimes(2).isInQcm)
        val p = wrongTimes(3)
        assertTrue(p.isInQcm)
        assertEquals(2, p.attemptsRemaining)
        assertEquals(card.qcmOptions.toSet(), p.qcmOrder.toSet())
    }

    @Test fun `qcm right answer gives one star`() {
        val p = CardRules.applyQcm(wrongTimes(3), "Porquerolles", card)
        assertEquals(Outcome.FOUND, p.outcome); assertEquals(1, p.stars)
    }

    @Test fun `two wrong qcm choices miss the card and keep both greyed`() {
        val p1 = CardRules.applyQcm(wrongTimes(3), "Split", card)
        assertNull(p1.outcome); assertEquals(listOf("Split"), p1.qcmWrong)
        val p2 = CardRules.applyQcm(p1, "Giens", card)
        assertEquals(Outcome.MISSED, p2.outcome); assertEquals(0, p2.stars)
        assertEquals(listOf("Split", "Giens"), p2.qcmWrong)
    }

    @Test fun `giving back an attempt cancels a missed card`() {
        val missed = CardRules.applyQcm(CardRules.applyQcm(wrongTimes(3), "Split", card), "Giens", card)
        val p = CardRules.giveBackAttempt(missed)
        assertNull(p.outcome); assertEquals(1, p.attemptsRemaining); assertTrue(p.isInQcm)
    }

    @Test fun `giving back an attempt from qcm start returns to free text`() {
        val p = CardRules.giveBackAttempt(wrongTimes(3))
        assertFalse(p.isInQcm); assertTrue(p.qcmOrder.isEmpty()); assertEquals(3, p.attemptsRemaining)
    }

    @Test fun `manual validation stars follow the refused attempt`() {
        assertEquals(3, CardRules.validateManually(wrongTimes(1)).stars)
        assertEquals(2, CardRules.validateManually(wrongTimes(3)).stars)
        assertEquals(1, CardRules.validateManually(CardRules.applyQcm(wrongTimes(3), "Split", card)).stars)
    }

    @Test fun `cards unlock at midnight on their day and stay open`() {
        assertFalse(AdventCalendar.isUnlocked(5, LocalDate.of(2026, 12, 4)))
        assertTrue(AdventCalendar.isUnlocked(5, LocalDate.of(2026, 12, 5)))
        assertTrue(AdventCalendar.isUnlocked(5, LocalDate.of(2027, 1, 10)))
        assertFalse(AdventCalendar.isUnlocked(1, LocalDate.of(2026, 11, 30)))
    }
}
