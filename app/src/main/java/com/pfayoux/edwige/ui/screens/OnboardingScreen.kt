package com.pfayoux.edwige.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pfayoux.edwige.GameViewModel
import com.pfayoux.edwige.ui.components.Postcard
import com.pfayoux.edwige.ui.components.Stars
import com.pfayoux.edwige.ui.theme.ChristmasRed
import com.pfayoux.edwige.ui.theme.InkSoft
import com.pfayoux.edwige.ui.theme.Paper

/** Premier lancement : les règles, puis la permission des notifications. */
@Composable
fun OnboardingScreen(vm: GameViewModel) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.finishOnboarding(notificationsEnabled = granted)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Stars(3, size = 32.dp)
        Text(
            vm.content.titre,
            style = MaterialTheme.typography.headlineLarge,
            color = ChristmasRed,
            textAlign = TextAlign.Center,
        )
        Text(
            "24 cartes, 24 souvenirs. Pour chacune, retrouve le lieu où la photo a été prise.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (vm.content.regles.isNotBlank()) {
            Postcard("La règle du jeu", vm.content.regles)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Je te préviendrai chaque matin à 9 h quand une nouvelle carte t'attend.",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= 33) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    vm.finishOnboarding(notificationsEnabled = true)
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
        ) { Text("Oui, préviens-moi !") }
        TextButton(onClick = { vm.finishOnboarding(notificationsEnabled = false) }) {
            Text("Non merci, je viendrai toute seule")
        }
    }
}
