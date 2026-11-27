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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.Feedback
import com.pfayoux.edwige.FeedbackMessage
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.R
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

    Scaffold(topBar = { BackTopBar(stringResource(R.string.card_title, day)) { vm.back() } }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Postcard(stringResource(R.string.card_title, day), card.texte)

            Text(
                stringResource(R.string.answer_question),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pluralStringResource(R.plurals.answer_attempts_remaining, progress.attemptsRemaining, progress.attemptsRemaining),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp),
                )
                AttemptsDots(progress.attemptsRemaining)
            }

            if (progress.isInQcm) {
                Text(
                    stringResource(R.string.answer_qcm_intro),
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
                    label = { Text(stringResource(R.string.answer_input_label)) },
                    placeholder = { Text(stringResource(R.string.answer_input_placeholder)) },
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
                    Text(stringResource(R.string.answer_submit), style = MaterialTheme.typography.titleMedium)
                }
            }

            AnimatedContent(
                targetState = feedback,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut() },
                label = "feedback",
            ) { fb ->
                if (fb != null) FeedbackBanner(fb) else Spacer(Modifier.height(1.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeedbackBanner(feedback: Feedback) {
    val (titleRes, color) = when (feedback.verdict) {
        Verdict.CORRECT -> R.string.feedback_title_correct to FirGreen
        Verdict.PRESQUE -> R.string.feedback_title_almost to Gold
        Verdict.TROP_VAGUE -> R.string.feedback_title_too_vague to Gold
        Verdict.FAUX -> R.string.feedback_title_wrong to ChristmasRed
    }
    val title = stringResource(titleRes)
    val message = feedbackMessage(feedback)
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

@Composable
private fun feedbackMessage(feedback: Feedback): String {
    fun pickFrom(list: Array<String>) = list[feedback.pick % list.size]
    return when (feedback.message) {
        FeedbackMessage.ALMOST -> pickFrom(stringArrayResource(R.array.feedback_almost))
        FeedbackMessage.TOO_VAGUE -> pickFrom(stringArrayResource(R.array.feedback_too_vague))
        FeedbackMessage.WRONG -> pickFrom(stringArrayResource(R.array.feedback_wrong))
        FeedbackMessage.WRONG_THEN_QCM ->
            stringResource(R.string.feedback_wrong_then_qcm, pickFrom(stringArrayResource(R.array.feedback_wrong)))
        FeedbackMessage.WRONG_LAST_CHANCE ->
            stringResource(R.string.feedback_wrong_last_chance, pickFrom(stringArrayResource(R.array.feedback_wrong)))
    }
}
