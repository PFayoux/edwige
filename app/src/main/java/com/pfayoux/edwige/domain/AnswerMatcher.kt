package com.pfayoux.edwige.domain

import java.text.Normalizer
import java.util.Locale

enum class Verdict { CORRECT, PRESQUE, TROP_VAGUE, FAUX }

/**
 * Juge une réponse en champ libre.
 *
 * Règles (voir spec.md, section 8) :
 * - normalisation : casse, accents, ponctuation et petits mots (articles, prépositions) ignorés ;
 * - la réponse peut être noyée dans une phrase ;
 * - tolérance aux fautes selon la longueur de la réponse attendue ;
 * - ordre : acceptée → trop vague → presque → faux.
 *
 * [knownPlaces] sert à ne pas dire « Bonne piste » quand la joueuse nomme un autre lieu
 * (par exemple « musée du Belvédère » contient le mot vague « musée », mais c'est faux).
 */
class AnswerMatcher(knownPlaces: Collection<String>) {

    private val knownPlaces: List<List<String>> =
        knownPlaces.map(::tokenize).filter { it.isNotEmpty() }.distinct()

    fun evaluate(input: String, card: Card): Verdict {
        val tokens = tokenize(input)
        if (tokens.isEmpty()) return Verdict.FAUX

        val accepted = card.acceptedAnswers.map(::tokenize).filter { it.isNotEmpty() }
        val vague = card.vagueAnswers.map(::tokenize).filter { it.isNotEmpty() }

        if (accepted.any { matches(tokens, it, correctThreshold(it)) }) return Verdict.CORRECT

        if (vague.any { matches(tokens, it, correctThreshold(it)) } && !namesAnotherPlace(tokens, card, accepted, vague)) {
            return Verdict.TROP_VAGUE
        }

        if (accepted.any { matches(tokens, it, almostThreshold(it)) }) return Verdict.PRESQUE

        return Verdict.FAUX
    }

    private fun namesAnotherPlace(
        tokens: List<String>,
        card: Card,
        accepted: List<List<String>>,
        vague: List<List<String>>,
    ): Boolean {
        val ownAnswer = tokenize(card.qcmAnswer)
        return knownPlaces
            .filter { it != ownAnswer && it !in accepted && it !in vague }
            .any { matches(tokens, it, correctThreshold(it)) }
    }

    private fun matches(input: List<String>, answer: List<String>, maxDistance: Int): Boolean =
        bestDistance(input, answer) <= maxDistance

    companion object {
        private val STOPWORDS = setOf(
            "le", "la", "les", "l", "de", "du", "des", "d", "a", "au", "aux", "en", "sur", "the", "of",
        )

        fun tokenize(text: String): List<String> {
            val lower = text.lowercase(Locale.FRENCH)
                .replace("œ", "oe").replace("æ", "ae").replace("ß", "ss")
            val withoutAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
            return withoutAccents.split(Regex("[^a-z0-9]+"))
                .filter { it.isNotEmpty() && it !in STOPWORDS }
        }

        private fun letters(answer: List<String>) = answer.sumOf { it.length }

        /** Fautes tolérées pour une réponse correcte. */
        fun correctThreshold(answer: List<String>): Int = when (letters(answer)) {
            in 0..5 -> 0
            in 6..9 -> 1
            else -> 2
        }

        /** Fautes au-delà desquelles ce n'est même plus « presque ». */
        fun almostThreshold(answer: List<String>): Int = when (letters(answer)) {
            in 0..5 -> 1
            in 6..9 -> 2
            else -> 4
        }

        /**
         * Plus petite distance entre la réponse attendue et un groupe de mots consécutifs
         * de la saisie (un mot de plus ou de moins toléré, espaces ignorés possibles).
         */
        fun bestDistance(input: List<String>, answer: List<String>): Int {
            val target = answer.joinToString(" ")
            val targetCompact = answer.joinToString("")
            var best = Int.MAX_VALUE
            val k = answer.size
            for (size in maxOf(1, k - 1)..(k + 1)) {
                if (size > input.size) continue
                for (start in 0..(input.size - size)) {
                    val window = input.subList(start, start + size)
                    val d = minOf(
                        levenshtein(window.joinToString(" "), target),
                        levenshtein(window.joinToString(""), targetCompact),
                    )
                    if (d < best) best = d
                }
            }
            return best
        }

        fun levenshtein(a: String, b: String): Int {
            if (a == b) return 0
            var prev = IntArray(b.length + 1) { it }
            var curr = IntArray(b.length + 1)
            for (i in 1..a.length) {
                curr[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    curr[j] = minOf(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost)
                }
                val tmp = prev; prev = curr; curr = tmp
            }
            return prev[b.length]
        }

        fun forContent(content: CalendarContent): AnswerMatcher = AnswerMatcher(
            content.cartes.flatMap { it.qcmOptions + it.acceptedAnswers },
        )
    }
}
