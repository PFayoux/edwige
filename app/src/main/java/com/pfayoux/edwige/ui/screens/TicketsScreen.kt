package com.pfayoux.edwige.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.R
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.ui.components.SoftNote
import com.pfayoux.edwige.ui.components.Ticket
import com.pfayoux.edwige.ui.theme.InkSoft

@Composable
fun TicketsScreen(vm: GameViewModel) {
    val won = (1..AdventCalendar.DAYS).filter { vm.progress(it).isFound }
    var confirmDay by remember { mutableStateOf<Int?>(null) }

    Scaffold(topBar = { BackTopBar(stringResource(R.string.tickets_title)) { vm.back() } }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (won.isEmpty()) {
                SoftNote(stringResource(R.string.tickets_empty))
            } else {
                val toUse = won.count { !vm.progress(it).ticketUsed }
                Text(
                    pluralStringResource(R.plurals.tickets_summary, won.size, won.size, toUse),
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkSoft,
                )
            }
            won.forEach { day ->
                val used = vm.progress(day).ticketUsed
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Ticket(vm.content.card(day), used = used)
                    if (!used) {
                        TextButton(onClick = { confirmDay = day }) { Text(stringResource(R.string.tickets_mark_used)) }
                    }
                }
            }
        }
    }

    confirmDay?.let { day ->
        AlertDialog(
            onDismissRequest = { confirmDay = null },
            title = { Text(stringResource(R.string.tickets_confirm_title)) },
            text = { Text(stringResource(R.string.tickets_confirm_text, vm.content.card(day).cadeau)) },
            confirmButton = {
                TextButton(onClick = { vm.markTicketUsed(day); confirmDay = null }) { Text(stringResource(R.string.tickets_confirm_yes)) }
            },
            dismissButton = { TextButton(onClick = { confirmDay = null }) { Text(stringResource(R.string.tickets_confirm_no)) } },
        )
    }
}

