package com.pfayoux.edwige.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.Screen
import com.pfayoux.edwige.domain.Messages
import com.pfayoux.edwige.ui.components.Confetti
import com.pfayoux.edwige.ui.components.Postcard
import com.pfayoux.edwige.ui.components.SoftNote
import com.pfayoux.edwige.ui.components.Stars
import com.pfayoux.edwige.ui.components.Ticket
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.InkSoft

/** Révélation du cadeau (celebrate = true) ou carte jouée rouverte en lecture seule. */
@Composable
fun RevealScreen(vm: GameViewModel, day: Int, celebrate: Boolean) {
    val card = vm.content.card(day)
    val progress = vm.progress(day)
    val missedMessage = remember(day) { Messages.missed[day % Messages.missed.size] }
    val correctMessage = remember(day) { Messages.pick(Messages.correct) }
    val ticketVisible = remember { MutableTransitionState(!celebrate).apply { targetState = true } }

    Box(Modifier.fillMaxSize()) {
        Scaffold(topBar = { BackTopBar("Carte n° $day") { vm.back() } }) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "${card.shortPlace} !",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (progress.isFound) ChristmasRed else InkSoft,
                    textAlign = TextAlign.Center,
                )
                if (card.lieu != card.shortPlace) {
                    Text(card.lieu, style = MaterialTheme.typography.titleMedium, color = InkSoft, textAlign = TextAlign.Center)
                }

                if (progress.isFound) {
                    if (celebrate) {
                        Text(correctMessage, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    }
                    Stars(progress.stars, size = 36.dp)
                    AnimatedVisibility(
                        visibleState = ticketVisible,
                        enter = fadeIn() + slideInVertically { it / 2 } +
                            scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    ) {
                        Ticket(card, used = progress.ticketUsed)
                    }
                } else {
                    SoftNote(missedMessage)
                }

                Postcard("Carte n° $day", card.texte)

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { vm.goHome() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Retour au calendrier") }
                if (progress.isFound) {
                    OutlinedButton(
                        onClick = { vm.goHome(); vm.navigate(Screen.Tickets) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Voir mes tickets") }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        if (celebrate && progress.isFound) Confetti()
    }
}
