package com.pfayoux.edwige.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class AnswerMatcherTest {

    private val content = CalendarContent.parse(File("src/main/assets/calendrier_avent.json").readText())
    private val matcher = AnswerMatcher.forContent(content)

    private fun verdict(day: Int, input: String) = matcher.evaluate(input, content.card(day))

    @Test fun `exact answer is correct`() = assertEquals(Verdict.CORRECT, verdict(15, "Porquerolles"))

    @Test fun `case and accents are ignored`() = assertEquals(Verdict.CORRECT, verdict(8, "SCHÖNBRUNN"))

    @Test fun `answer without accent is correct`() = assertEquals(Verdict.CORRECT, verdict(8, "schonbrunn"))

    @Test fun `articles and hyphens are ignored`() {
        assertEquals(Verdict.CORRECT, verdict(1, "la plage de Split"))
        assertEquals(Verdict.CORRECT, verdict(16, "St Tropez"))
        assertEquals(Verdict.CORRECT, verdict(13, "Cergy-Pontoise"))
        assertEquals(Verdict.CORRECT, verdict(17, "Villajoyosa"))
    }

    @Test fun `answer inside a sentence is correct`() =
        assertEquals(Verdict.CORRECT, verdict(22, "je crois que c'était à Plitvise"))

    @Test fun `one typo on a medium word is correct`() = assertEquals(Verdict.CORRECT, verdict(15, "Porquerole"))

    @Test fun `two typos on a long word is correct`() =
        assertEquals(Verdict.CORRECT, verdict(7, "Kunsthistoriches musem"))

    @Test fun `no typo tolerated on short names`() {
        assertEquals(Verdict.PRESQUE, verdict(5, "Tour"))
        assertEquals(Verdict.PRESQUE, verdict(21, "Pari"))
        assertEquals(Verdict.CORRECT, verdict(5, "Tours"))
    }

    @Test fun `two typos on a medium word is almost`() = assertEquals(Verdict.PRESQUE, verdict(15, "Porkerole"))

    @Test fun `city alone on a precise card is too vague`() {
        assertEquals(Verdict.TROP_VAGUE, verdict(7, "Vienne"))
        assertEquals(Verdict.TROP_VAGUE, verdict(8, "à Vienne"))
        assertEquals(Verdict.TROP_VAGUE, verdict(9, "le marché de Noël"))
        assertEquals(Verdict.TROP_VAGUE, verdict(14, "Paris"))
        assertEquals(Verdict.TROP_VAGUE, verdict(14, "l'arc de triomphe"))
        assertEquals(Verdict.TROP_VAGUE, verdict(24, "Bercy"))
    }

    @Test fun `precise place wins over vague city in the same sentence`() {
        assertEquals(Verdict.CORRECT, verdict(8, "Vienne, au château de Schönbrunn"))
        assertEquals(Verdict.CORRECT, verdict(14, "arc de triomphe du Carrousel"))
        assertEquals(Verdict.CORRECT, verdict(9, "marché de Noël de Schönbrunn"))
        assertEquals(Verdict.CORRECT, verdict(24, "les pavillons de Bercy"))
    }

    @Test fun `naming another place is wrong, not vague`() {
        assertEquals(Verdict.FAUX, verdict(7, "le musée du Belvédère"))
        assertEquals(Verdict.FAUX, verdict(14, "Arc de triomphe de l'Étoile"))
        assertEquals(Verdict.FAUX, verdict(24, "Kunsthistorisches Museum"))
    }

    @Test fun `wrong answers are wrong`() {
        assertEquals(Verdict.FAUX, verdict(1, "Marseille"))
        assertEquals(Verdict.FAUX, verdict(5, "Rouen"))
        assertEquals(Verdict.FAUX, verdict(3, "Alicante"))
        assertEquals(Verdict.FAUX, verdict(20, "Poitiers"))
    }

    @Test fun `blank input is wrong`() = assertEquals(Verdict.FAUX, verdict(1, "   "))

    @Test fun `every accepted answer of every card is correct`() {
        content.cartes.forEach { card ->
            (card.acceptedAnswers + card.qcmAnswer + card.lieu).forEach { answer ->
                assertEquals("carte ${card.jour} : « $answer »", Verdict.CORRECT, matcher.evaluate(answer, card))
            }
        }
    }

    @Test fun `no wrong qcm option is accepted as correct`() {
        content.cartes.forEach { card ->
            card.qcmWrongAnswers.forEach { answer ->
                val v = matcher.evaluate(answer, card)
                assert(v != Verdict.CORRECT) { "carte ${card.jour} : « $answer » accepté à tort" }
            }
        }
    }

    @Test fun `content has 24 cards with 3 wrong options each`() {
        assertEquals((1..24).toList(), content.cartes.map { it.jour })
        content.cartes.forEach { assertEquals("carte ${it.jour}", 3, it.qcmWrongAnswers.distinct().size) }
    }
}
