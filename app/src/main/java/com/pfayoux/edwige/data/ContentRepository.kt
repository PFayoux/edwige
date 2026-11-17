package com.pfayoux.edwige.data

import android.content.Context
import com.pfayoux.edwige.domain.CalendarContent

object ContentRepository {
    @Volatile private var cached: CalendarContent? = null

    fun load(context: Context): CalendarContent = cached ?: synchronized(this) {
        cached ?: CalendarContent.parse(
            context.assets.open("calendrier_avent.json").bufferedReader().use { it.readText() },
        ).also { cached = it }
    }
}
