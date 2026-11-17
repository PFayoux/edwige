package com.pfayoux.edwige.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.domain.Verdict
import com.pfayoux.edwige.ui.components.AttemptsDots
import com.pfayoux.edwige.ui.components.Postcard
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.Faded
import com.pfayoux.edwige.ui.theme.FirGreen
import com.pfayoux.edwige.ui.theme.Gold
import com.pfayoux.edwige.ui.theme.Ink
import com.pfayoux.edwige.ui.theme.InkSoft
import com.pfayoux.edwige.ui.theme.PaperDeep

@Composable
fun AnswerScreen(vm: GameViewModel, day: Int) {
    val card = vm.content.card(day)
    val progress = vm.progress(day)
    var input by rememberSaveable(day) { mutableStateOf("") }
    val feedback = vm.feedback?.takeIf { it.day == day }

    // Après une réponse fausse, on vide le champ ; après « presque », on le garde pour corriger.
    LaunchedEffect(feedback?.id) {
        if (feedback?.verdict == Verdict.FAUX) input = ""
    }

    Scaffold(topBar = { BackTopBar("Carte n° $day") { vm.back() } }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Postcard("Carte n° $day", card.texte)

            Text(
                "Où cette photo a-t-elle été prise ?",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Tentatives restantes : ${progress.attemptsRemaining}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp),
                )
                AttemptsDots(progress.attemptsRemaining)
            }

            if (progress.isInQcm) {
                Text(
                    "Dernière ligne droite : choisis le bon lieu.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkSoft,
                )
                progress.qcmOrder.forEach { option ->
                    val greyed = option in progress.qcmWrong
                    OutlinedButton(
                        onClick = { vm.chooseQcm(day, option) },
                        enabled = !greyed,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, if (greyed) Faded else ChristmasRed),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Ink,
                            disabledContainerColor = PaperDeep,
                            disabledContentColor = Faded,
                        ),
                    ) {
                        Text(
                            option,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            textDecoration = if (greyed) TextDecoration.LineThrough else null,
                        )
                    }
                }
            } else {
                val submit = { if (input.isNotBlank()) vm.submitFreeText(day, input) }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Le lieu") },
                    placeholder = { Text("Ville, monument, plage…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                Button(
                    onClick = submit,
                    enabled = input.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Valider", style = MaterialTheme.typography.titleMedium)
                }
            }

            AnimatedContent(
                targetState = feedback,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut() },
                label = "feedback",
            ) { fb ->
                if (fb != null) FeedbackBanner(fb.verdict, fb.message) else Spacer(Modifier.height(1.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeedbackBanner(verdict: Verdict, message: String) {
    val (title, color) = when (verdict) {
        Verdict.CORRECT -> "Correct !" to FirGreen
        Verdict.PRESQUE -> "Presque !" to Gold
        Verdict.TROP_VAGUE -> "Plus précis !" to Gold
        Verdict.FAUX -> "Raté" to ChristmasRed
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = color)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}
