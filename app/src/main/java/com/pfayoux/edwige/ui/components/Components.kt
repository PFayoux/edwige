package com.pfayoux.edwige.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfayoux.edwige.R
import com.pfayoux.edwige.domain.Card
import com.pfayoux.edwige.domain.CardRules
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.Faded
import com.pfayoux.edwige.ui.theme.FirGreen
import com.pfayoux.edwige.ui.theme.Gold
import com.pfayoux.edwige.ui.theme.Ink
import com.pfayoux.edwige.ui.theme.InkSoft
import com.pfayoux.edwige.ui.theme.PaperDeep
import com.pfayoux.edwige.ui.theme.PostcardText
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun Stars(count: Int, modifier: Modifier = Modifier, size: Dp = 18.dp, max: Int = 3) {
    val description = pluralStringResource(R.plurals.stars_description, count, count, max)
    Row(
        modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(max) { i ->
            Icon(
                if (i < count) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (i < count) Gold else Faded,
                modifier = Modifier.size(size),
            )
        }
    }
}

/** ●●●○○ : les tentatives restantes. */
@Composable
fun AttemptsDots(remaining: Int, modifier: Modifier = Modifier) {
    val description = pluralStringResource(R.plurals.attempts_dots_description, remaining, remaining, CardRules.MAX_ATTEMPTS)
    Row(
        modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(CardRules.MAX_ATTEMPTS) { i ->
            val filled = i < remaining
            Box(
                Modifier
                    .size(14.dp)
                    .background(if (filled) ChristmasRed else Color.Transparent, CircleShape)
                    .border(2.dp, if (filled) ChristmasRed else Faded, CircleShape),
            )
        }
    }
}

/** La carte postale : le texte imprimé sur la carte physique, en rappel. */
@Composable
fun Postcard(header: String, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, PaperDeep),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                header,
                style = MaterialTheme.typography.labelLarge,
                color = ChristmasRed,
            )
            Spacer(Modifier.height(8.dp))
            Text(text, style = PostcardText.copy(fontSize = 17.sp, lineHeight = 25.sp), color = Ink)
        }
    }
}

/** Forme de ticket : coins arrondis et deux encoches en demi-cercle sur les côtés. */
private object TicketShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val corner = with(density) { 10.dp.toPx() }
        val notch = with(density) { 12.dp.toPx() }
        val body = Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, corner, corner))
        }
        val notches = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(Offset(0f, size.height / 2), notch))
            addOval(androidx.compose.ui.geometry.Rect(Offset(size.width, size.height / 2), notch))
        }
        return Outline.Generic(Path.combine(PathOperation.Difference, body, notches))
    }
}

/** Un ticket cadeau, avec le tampon « Utilisé » s'il a servi. */
@Composable
fun Ticket(card: Card, used: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Surface(
            color = if (used) PaperDeep else Color.White,
            shape = TicketShape,
            border = BorderStroke(2.dp, if (used) Faded else Gold),
            shadowElevation = if (used) 0.dp else 3.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.ticket_number, card.jour),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = if (used) InkSoft else ChristmasRed,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    card.cadeau,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (used) InkSoft else Ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                DashedLine(if (used) Faded else Gold)
                Spacer(Modifier.height(8.dp))
                Text(
                    card.shortPlace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (used) {
            Text(
                stringResource(R.string.ticket_used_stamp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(-14f)
                    .border(3.dp, ChristmasRed.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                color = ChristmasRed.copy(alpha = 0.75f),
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                letterSpacing = 4.sp,
            )
        }
    }
}

@Composable
private fun DashedLine(color: Color) {
    Canvas(Modifier.fillMaxWidth().height(2.dp)) {
        drawLine(
            color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
        )
    }
}

private data class Confetto(val x: Float, val delay: Float, val speed: Float, val sway: Float, val size: Float, val color: Color, val spin: Float)

/** Pluie de confettis, une seule fois, sur toute la surface. */
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val pieces = remember {
        val palette = listOf(ChristmasRed, FirGreen, Gold, Color(0xFFE8A0A0), Color(0xFF8FB7A5))
        val r = Random(System.nanoTime())
        List(90) {
            Confetto(
                x = r.nextFloat(), delay = r.nextFloat() * 0.35f, speed = 0.7f + r.nextFloat() * 0.6f,
                sway = r.nextFloat() * 2f * PI.toFloat(), size = 6f + r.nextFloat() * 8f,
                color = palette[r.nextInt(palette.size)], spin = r.nextFloat() * 360f,
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(4000, easing = LinearEasing)) }
    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        pieces.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f) * p.speed
            if (local <= 0f || local > 1.1f) return@forEach
            val y = -20f + local * (size.height + 40f)
            val x = p.x * size.width + sin(local * 8f + p.sway) * 30f
            val alpha = (1f - t).coerceIn(0f, 1f) * 1.5f
            drawRect(
                p.color.copy(alpha = alpha.coerceAtMost(1f)),
                topLeft = Offset(x, y),
                size = Size(p.size, p.size * 0.6f),
            )
        }
    }
}

/** Un bandeau doux pour les états vides ou les infos. */
@Composable
fun SoftNote(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = PaperDeep, shape = RoundedCornerShape(12.dp)) {
        Text(
            text, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge,
            color = InkSoft, textAlign = TextAlign.Center,
        )
    }
}

