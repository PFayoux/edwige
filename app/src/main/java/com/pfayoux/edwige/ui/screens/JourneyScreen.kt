package com.pfayoux.edwige.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.R
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.ui.components.Confetti
import com.pfayoux.edwige.ui.components.Stars
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.Faded
import com.pfayoux.edwige.ui.theme.Gold
import com.pfayoux.edwige.ui.theme.Ink
import com.pfayoux.edwige.ui.theme.InkSoft

/** Fin du calendrier : le carnet de voyage des 24 lieux. */
@Composable
fun JourneyScreen(vm: GameViewModel) {
    Box(Modifier.fillMaxSize()) {
        Scaffold(topBar = { BackTopBar(stringResource(R.string.journey_title)) { vm.back() } }) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.journey_merry_christmas), style = MaterialTheme.typography.displaySmall, color = ChristmasRed, textAlign = TextAlign.Center)
                Text(
                    stringResource(
                        R.string.journey_summary,
                        pluralStringResource(R.plurals.journey_places, vm.foundCount, vm.foundCount),
                        AdventCalendar.DAYS,
                        pluralStringResource(R.plurals.journey_stars, vm.totalStars, vm.totalStars),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(stringResource(R.string.journey_steps_intro), style = MaterialTheme.typography.bodyLarge, color = InkSoft)
                Spacer(Modifier.height(4.dp))
                (1..AdventCalendar.DAYS).forEach { day ->
                    val card = vm.content.card(day)
                    val progress = vm.progress(day)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$day",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (progress.isFound) Gold else Faded,
                            modifier = Modifier.widthIn(min = 36.dp),
                        )
                        Text(
                            card.lieu,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (progress.isFound) Ink else InkSoft,
                            modifier = Modifier.weight(1f),
                        )
                        if (progress.isFound) Stars(progress.stars, size = 14.dp)
                    }
                    HorizontalDivider(color = Faded.copy(alpha = 0.4f))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        Confetti()
    }
}
