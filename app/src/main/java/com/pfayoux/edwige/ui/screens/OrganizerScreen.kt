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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.R
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.domain.CardStatus
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.Faded
import com.pfayoux.edwige.ui.theme.InkSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizerScreen(vm: GameViewModel) {
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(topBar = { BackTopBar(stringResource(R.string.organizer_title)) { vm.back() } }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.organizer_date_section), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(
                    if (vm.isDateSimulated) R.string.organizer_date_simulated else R.string.organizer_date_real,
                    vm.today.dayOfMonth, vm.today.monthValue, vm.today.year,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.shiftSimulatedDate(-1) }) { Text(stringResource(R.string.organizer_day_minus)) }
                OutlinedButton(onClick = { vm.shiftSimulatedDate(1) }) { Text(stringResource(R.string.organizer_day_plus)) }
                OutlinedButton(onClick = { vm.setSimulatedDate(AdventCalendar.unlockDate(1)) }) { Text(stringResource(R.string.organizer_first_december)) }
                OutlinedButton(onClick = { vm.unlockAll() }) { Text(stringResource(R.string.organizer_unlock_all)) }
                OutlinedButton(onClick = { vm.setSimulatedDate(null) }) { Text(stringResource(R.string.organizer_real_date)) }
            }
            Text(
                stringResource(R.string.organizer_date_note),
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
            )

            HorizontalDivider()
            Text(stringResource(R.string.organizer_notifications_section), style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { vm.testNotification() }) { Text(stringResource(R.string.organizer_test_notification)) }

            HorizontalDivider()
            Text(stringResource(R.string.organizer_cards_section), style = MaterialTheme.typography.titleLarge)
            (1..AdventCalendar.DAYS).forEach { day ->
                val card = vm.content.card(day)
                val p = vm.progress(day)
                val state = when (vm.status(day)) {
                    CardStatus.FOUND -> stringResource(
                        if (p.ticketUsed) R.string.organizer_status_found_ticket_used else R.string.organizer_status_found,
                        p.stars,
                    )
                    CardStatus.MISSED -> stringResource(R.string.organizer_status_missed)
                    CardStatus.LOCKED -> stringResource(R.string.organizer_status_locked)
                    CardStatus.AVAILABLE -> if (p.attemptsUsed == 0) {
                        stringResource(R.string.organizer_status_not_started)
                    } else {
                        pluralStringResource(R.plurals.organizer_status_attempts_remaining, p.attemptsRemaining, p.attemptsRemaining)
                    }
                }
                Column(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.organizer_card_row, day, card.shortPlace), style = MaterialTheme.typography.titleMedium)
                    Text(state, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!p.isFound) {
                            TextButton(onClick = { vm.validateManually(day) }) { Text(stringResource(R.string.organizer_validate)) }
                        }
                        if (!p.isFound && p.attemptsUsed > 0) {
                            TextButton(onClick = { vm.giveBackAttempt(day) }) { Text(stringResource(R.string.organizer_give_back_attempt)) }
                        }
                    }
                }
                HorizontalDivider(color = Faded.copy(alpha = 0.4f))
            }

            Text(stringResource(R.string.organizer_reset_section), style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = { confirmReset = true },
                colors = ButtonDefaults.buttonColors(containerColor = ChristmasRed),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.organizer_reset)) }
            Text(
                stringResource(R.string.organizer_reset_note),
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.organizer_reset_confirm_title)) },
            text = { Text(stringResource(R.string.organizer_reset_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; vm.resetEverything() }) { Text(stringResource(R.string.organizer_reset_confirm_yes), color = ChristmasRed) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.organizer_reset_confirm_no)) } },
        )
    }
}
