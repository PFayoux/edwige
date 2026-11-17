package com.pfayoux.edwige.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Carnet de voyage + Noël : papier, encre, rouge de Noël, vert sapin, or.
val Paper = Color(0xFFFBF5E9)
val PaperDeep = Color(0xFFF1E5CC)
val Ink = Color(0xFF2E2A26)
val InkSoft = Color(0xFF6B6259)
val ChristmasRed = Color(0xFFB3342E)
val FirGreen = Color(0xFF2F5D50)
val Gold = Color(0xFFD4A72C)
val Faded = Color(0xFFB9B0A3)

private val colors = lightColorScheme(
    primary = ChristmasRed,
    onPrimary = Paper,
    secondary = FirGreen,
    onSecondary = Paper,
    primaryContainer = Color(0xFFF6DAD6),
    onPrimaryContainer = Ink,
    secondaryContainer = PaperDeep,
    onSecondaryContainer = Ink,
    tertiaryContainer = Color(0xFFF7EBC8),
    onTertiaryContainer = Ink,
    surfaceContainer = Paper,
    surfaceContainerHigh = Paper,
    surfaceContainerHighest = PaperDeep,
    surfaceContainerLow = Paper,
    surfaceContainerLowest = Color.White,
    tertiary = Gold,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = InkSoft,
    outline = Faded,
)

private val base = Typography()
private val serif = FontFamily.Serif

private val typography = base.copy(
    displaySmall = base.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = serif, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
)

/** Texte manuscrit des cartes postales. */
val PostcardText = TextStyle(fontFamily = serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

@Composable
fun EdwigeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
