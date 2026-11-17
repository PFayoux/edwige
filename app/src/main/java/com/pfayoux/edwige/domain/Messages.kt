package com.pfayoux.edwige.domain

import kotlin.random.Random

/** Messages variés, toujours bienveillants. */
object Messages {
    val correct = listOf(
        "Bravo, tu t'en souviens !",
        "Exactement ! Quelle mémoire !",
        "Oui ! C'était bien là.",
        "Gagné ! Tu m'impressionnes.",
        "Dans le mille !",
    )
    val almost = listOf(
        "Vérifie l'orthographe, tu y es.",
        "Tout près ! Une petite lettre te joue des tours ?",
        "Relis bien, c'est à un cheveu.",
        "Ça y ressemble beaucoup… regarde l'orthographe !",
    )
    val tooVague = listOf(
        "Bonne piste, sois plus précise !",
        "Tu brûles ! Mais où exactement ?",
        "C'est bien la bonne ville… mais quel endroit précis ?",
    )
    val wrong = listOf(
        "Ce n'est pas ça, mais tu vas trouver !",
        "Raté, mais ne lâche rien.",
        "Hmm, pas tout à fait. Repense à ce jour-là…",
        "Non, mais je crois en toi !",
        "Pas encore ! Ferme les yeux, souviens-toi…",
    )
    val missed = listOf(
        "Ce cadeau-là s'envole… mais pas le souvenir !",
        "Pas grave : le souvenir, lui, reste à nous.",
        "Celle-ci t'a échappé, mais on y retournera peut-être !",
    )
    val notifications = listOf(
        "Une nouvelle carte t'attend !",
        "Ta carte du jour est prête à être devinée.",
        "Un nouveau souvenir à retrouver aujourd'hui !",
        "Debout, voyageuse : une carte t'attend !",
    )

    fun pick(list: List<String>, random: Random = Random): String = list.random(random)
}
