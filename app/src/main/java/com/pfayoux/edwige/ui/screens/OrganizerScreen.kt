package com.pfayoux.edwige.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.domain.CardStatus
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.Faded
import com.pfayoux.edwige.ui.theme.InkSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizerScreen(vm: GameViewModel) {
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(topBar = { BackTopBar("Mode organisateur") { vm.back() } }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Date", style = MaterialTheme.typography.titleLarge)
            Text(
                "Date utilisée : ${vm.today.dayOfMonth}/${vm.today.monthValue}/${vm.today.year}" +
                    if (vm.isDateSimulated) " (simulée)" else " (vraie date)",
                style = MaterialTheme.typography.bodyLarge,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.shiftSimulatedDate(-1) }) { Text("− 1 jour") }
                OutlinedButton(onClick = { vm.shiftSimulatedDate(1) }) { Text("+ 1 jour") }
                OutlinedButton(onClick = { vm.setSimulatedDate(AdventCalendar.unlockDate(1)) }) { Text("1er déc.") }
                OutlinedButton(onClick = { vm.unlockAll() }) { Text("Tout débloquer") }
                OutlinedButton(onClick = { vm.setSimulatedDate(null) }) { Text("Vraie date") }
            }
            Text(
                "La date simulée ne change que le déblocage des cartes. Les rappels suivent la vraie date.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
            )

            HorizontalDivider()
            Text("Notifications", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { vm.testNotification() }) { Text("Envoyer une notification de test") }

            HorizontalDivider()
            Text("Cartes", style = MaterialTheme.typography.titleLarge)
            (1..AdventCalendar.DAYS).forEach { day ->
                val card = vm.content.card(day)
                val p = vm.progress(day)
                val state = when (vm.status(day)) {
                    CardStatus.FOUND -> "trouvée, ${p.stars}★" + if (p.ticketUsed) ", ticket utilisé" else ""
                    CardStatus.MISSED -> "manquée"
                    CardStatus.LOCKED -> "verrouillée"
                    CardStatus.AVAILABLE -> if (p.attemptsUsed == 0) "pas commencée" else "${p.attemptsRemaining} tentative(s) restante(s)"
                }
                Column(Modifier.fillMaxWidth()) {
                    Text("$day · ${card.shortPlace}", style = MaterialTheme.typography.titleMedium)
                    Text(state, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!p.isFound) {
                            TextButton(onClick = { vm.validateManually(day) }) { Text("Valider") }
                        }
                        if (!p.isFound && p.attemptsUsed > 0) {
                            TextButton(onClick = { vm.giveBackAttempt(day) }) { Text("Rendre une tentative") }
                        }
                    }
                }
                HorizontalDivider(color = Faded.copy(alpha = 0.4f))
            }

            Text("Remise à zéro", style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = { confirmReset = true },
                colors = ButtonDefaults.buttonColors(containerColor = ChristmasRed),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Tout réinitialiser") }
            Text(
                "Efface toute la progression, les tickets, la date simulée et les réglages, comme une appli neuve.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Tout réinitialiser ?") },
            text = { Text("Toutes les réponses, étoiles et tickets seront effacés. C'est définitif.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; vm.resetEverything() }) { Text("Réinitialiser", color = ChristmasRed) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Annuler") } },
        )
    }
}
