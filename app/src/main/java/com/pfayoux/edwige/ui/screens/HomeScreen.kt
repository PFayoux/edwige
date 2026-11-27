package com.pfayoux.edwige.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.R
import com.pfayoux.edwige.Screen
import com.pfayoux.edwige.domain.AdventCalendar
import com.pfayoux.edwige.domain.CardStatus
import com.pfayoux.edwige.ui.components.SoftNote
import com.pfayoux.edwige.ui.components.Stars
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.Faded
import com.pfayoux.edwige.ui.theme.FirGreen
import com.pfayoux.edwige.ui.theme.Gold
import com.pfayoux.edwige.ui.theme.Ink
import com.pfayoux.edwige.ui.theme.InkSoft
import com.pfayoux.edwige.ui.theme.PaperDeep
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val ORGANIZER_PIN = "1337"
private const val ORGANIZER_HOLD_MILLIS = 3000L

@Composable
fun HomeScreen(vm: GameViewModel) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var askPin by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.setNotificationsEnabled(granted)
        if (!granted) scope.launch {
            snackbar.showSnackbar(context.getString(R.string.home_reminder_permission_denied))
        }
    }

    fun toggleNotifications() {
        val enable = !vm.save.notificationsEnabled
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (enable && needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.setNotificationsEnabled(enable)
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(context.getString(if (enable) R.string.home_reminder_enabled else R.string.home_reminder_disabled))
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ChristmasRed,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            // Appui long de 3 s : accès discret au mode organisateur.
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                val released = withTimeoutOrNull(ORGANIZER_HOLD_MILLIS) { waitForUpOrCancellation() }
                                if (released == null) askPin = true
                            }
                        },
                )
                IconButton(onClick = ::toggleNotifications) {
                    Icon(
                        if (vm.save.notificationsEnabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                        contentDescription = stringResource(if (vm.save.notificationsEnabled) R.string.home_reminder_disable else R.string.home_reminder_enable),
                        tint = InkSoft,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pluralStringResource(R.plurals.home_found_count, vm.foundCount, vm.foundCount, AdventCalendar.DAYS),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.size(12.dp))
                Text(stringResource(R.string.home_total_stars, vm.totalStars), style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
            }
            if (vm.isDateSimulated) {
                Text(
                    stringResource(R.string.home_simulated_date, vm.today.dayOfMonth, vm.today.monthValue, vm.today.year),
                    style = MaterialTheme.typography.labelMedium,
                    color = ChristmasRed,
                )
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = { vm.navigate(Screen.Tickets) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.tickets_title))
            }
            Spacer(Modifier.height(16.dp))

            if (vm.allPlayed) {
                Surface(
                    onClick = { vm.navigate(Screen.Journey) },
                    color = FirGreen,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.home_journey_complete),
                        Modifier.padding(16.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(16.dp))
            } else if (vm.today.isBefore(AdventCalendar.unlockDate(1))) {
                SoftNote(stringResource(R.string.home_before_december))
                Spacer(Modifier.height(16.dp))
            }

            CalendarGrid(vm) { day ->
                if (vm.status(day) == CardStatus.LOCKED) {
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(context.getString(R.string.home_card_locked, day))
                    }
                } else {
                    vm.openCard(day)
                }
            }
        }
    }

    if (askPin) {
        PinDialog(
            onDismiss = { askPin = false },
            onPin = { pin ->
                askPin = false
                if (pin == ORGANIZER_PIN) vm.navigate(Screen.Organizer)
            },
        )
    }
}

@Composable
private fun CalendarGrid(vm: GameViewModel, onClick: (Int) -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Des cases d'au moins 80 dp, plus larges si la police du téléphone est agrandie.
        val minCell = 80.dp * maxOf(1f, fontScale * 0.85f)
        val columns = (maxWidth / minCell).toInt().coerceIn(2, 6)
        val cellWidth = (maxWidth - CELL_SPACING * (columns - 1)) / columns
        Column(verticalArrangement = Arrangement.spacedBy(CELL_SPACING)) {
            (1..AdventCalendar.DAYS).chunked(columns).forEach { row ->
                // Hauteur intrinsèque : toutes les cases d'une rangée ont la même hauteur.
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(CELL_SPACING)) {
                    row.forEach { day ->
                        DayCell(vm, day, cellWidth - CELL_PADDING * 2, Modifier.weight(1f).fillMaxHeight(), onClick)
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

private val CELL_SPACING = 10.dp
private val CELL_PADDING = 5.dp

/**
 * Texte centré qui réduit sa taille (jusqu'à 70 %) pour que son mot le plus long tienne
 * sur une ligne : jamais de mot coupé au milieu.
 */
@Composable
private fun FitWordsText(text: String, maxWidth: Dp, style: TextStyle, color: Color, maxLines: Int = 2) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxPx = with(density) { maxWidth.roundToPx() }
    val longest = text.split(" ").maxByOrNull { it.length } ?: text
    val fitted = remember(text, maxPx, style, density) {
        var scale = 1f
        while (scale > 0.7f && measurer.measure(longest, style.copy(fontSize = style.fontSize * scale)).size.width > maxPx) {
            scale -= 0.05f
        }
        style.copy(fontSize = style.fontSize * scale)
    }
    Text(text, style = fitted, color = color, textAlign = TextAlign.Center, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun DayCell(vm: GameViewModel, day: Int, textWidth: Dp, modifier: Modifier, onClick: (Int) -> Unit) {
    val status = vm.status(day)
    val isToday = vm.todayCard == day && status == CardStatus.AVAILABLE
    val progress = vm.progress(day)
    val card = vm.content.card(day)

    val container = when (status) {
        CardStatus.LOCKED -> PaperDeep
        CardStatus.AVAILABLE -> if (isToday) ChristmasRed else Color.White
        CardStatus.FOUND -> Color.White
        CardStatus.MISSED -> PaperDeep
    }
    val content = when {
        isToday -> Color.White
        status == CardStatus.LOCKED || status == CardStatus.MISSED -> InkSoft
        else -> Ink
    }
    val border = when (status) {
        CardStatus.AVAILABLE -> BorderStroke(2.dp, ChristmasRed)
        CardStatus.FOUND -> BorderStroke(2.dp, Gold)
        else -> BorderStroke(1.dp, Faded)
    }
    val label = when (status) {
        CardStatus.LOCKED -> stringResource(R.string.home_cell_locked_description, day)
        CardStatus.AVAILABLE -> stringResource(
            if (isToday) R.string.home_cell_today_description else R.string.home_cell_to_play_description,
            day,
        )
        CardStatus.FOUND -> stringResource(R.string.home_cell_found_description, day, card.shortPlace)
        CardStatus.MISSED -> stringResource(R.string.home_cell_missed_description, day, card.shortPlace)
    }

    Surface(
        onClick = { onClick(day) },
        modifier = modifier
            .heightIn(min = 92.dp)
            .semantics { contentDescription = label },
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(10.dp),
        border = border,
        shadowElevation = if (status == CardStatus.AVAILABLE) 3.dp else 0.dp,
    ) {
        Column(
            Modifier
                .clearAndSetSemantics {}
                .padding(horizontal = CELL_PADDING - 2.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("$day", style = MaterialTheme.typography.headlineSmall, color = content)
            Spacer(Modifier.height(2.dp))
            when (status) {
                CardStatus.LOCKED -> Icon(Icons.Filled.Lock, contentDescription = null, tint = Faded, modifier = Modifier.size(18.dp))
                CardStatus.AVAILABLE -> FitWordsText(
                    when {
                        progress.isInQcm -> stringResource(R.string.home_cell_qcm)
                        progress.attemptsUsed > 0 -> stringResource(R.string.home_cell_in_progress)
                        isToday -> stringResource(R.string.home_cell_today)
                        else -> stringResource(R.string.home_cell_to_play)
                    },
                    textWidth,
                    MaterialTheme.typography.labelSmall,
                    content,
                )
                CardStatus.FOUND, CardStatus.MISSED -> {
                    FitWordsText(card.shortPlace, textWidth, MaterialTheme.typography.labelSmall, content)
                    if (status == CardStatus.FOUND) {
                        Spacer(Modifier.height(2.dp))
                        Stars(progress.stars, size = 12.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PinDialog(onDismiss: () -> Unit, onPin: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_pin_title)) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { value ->
                    val digits = value.filter(Char::isDigit).take(4)
                    pin = digits
                    if (digits.length == 4) onPin(digits)
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.home_pin_close)) } },
    )
}
