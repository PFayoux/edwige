package com.pfayoux.edwige.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Contenu du calendrier, tel qu'écrit dans `calendrier_avent.json`. */
@Serializable
data class CalendarContent(
    val titre: String,
    val regles: String = "",
    val cartes: List<Card>,
) {
    fun card(day: Int): Card = cartes.first { it.jour == day }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun parse(text: String): CalendarContent = json.decodeFromString(serializer(), text)
    }
}

@Serializable
data class Card(
    val jour: Int,
    /** Le lieu complet, affiché une fois révélé. */
    val lieu: String,
    @SerialName("reponses_acceptees") val acceptedAnswers: List<String>,
    @SerialName("reponses_trop_vagues") val vagueAnswers: List<String> = emptyList(),
    @SerialName("qcm_bonne_reponse") val qcmAnswer: String,
    @SerialName("qcm_mauvaises_reponses") val qcmWrongAnswers: List<String>,
    val texte: String,
    val cadeau: String,
) {
    /** Nom court du lieu, pour les espaces réduits (case du calendrier, carnet). */
    val shortPlace: String get() = qcmAnswer

    val qcmOptions: List<String> get() = listOf(qcmAnswer) + qcmWrongAnswers
}
