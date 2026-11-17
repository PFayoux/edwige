package com.pfayoux.edwige.data

import android.content.Context
import androidx.core.util.AtomicFile
import com.pfayoux.edwige.domain.CardProgress
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

/** Tout ce que l'appli retient entre deux lancements. */
@Serializable
data class SaveData(
    val cards: Map<Int, CardProgress> = emptyMap(),
    /** Date simulée du mode organisateur (ISO), ou null pour la vraie date. */
    val simulatedDate: String? = null,
    val notificationsEnabled: Boolean = true,
    val onboardingDone: Boolean = false,
) {
    fun progress(day: Int): CardProgress = cards[day] ?: CardProgress()
    fun withProgress(day: Int, progress: CardProgress) = copy(cards = cards + (day to progress))
    fun today(realToday: LocalDate): LocalDate = simulatedDate?.let(LocalDate::parse) ?: realToday
}

/** Où la progression est rangée. */
interface SaveStore {
    fun load(): SaveData
    fun save(data: SaveData)
}

/** Enregistre la progression dans un fichier, de façon atomique, après chaque changement. */
class ProgressStore(context: Context) : SaveStore {
    private val file = AtomicFile(File(context.applicationContext.filesDir, "progress.json"))
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun load(): SaveData = try {
        json.decodeFromString(SaveData.serializer(), file.readFully().decodeToString())
    } catch (e: Exception) {
        SaveData()
    }

    @Synchronized
    override fun save(data: SaveData) {
        val stream = file.startWrite()
        try {
            stream.write(json.encodeToString(SaveData.serializer(), data).encodeToByteArray())
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw e
        }
    }
}
