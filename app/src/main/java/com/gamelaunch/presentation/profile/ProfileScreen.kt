package com.gamelaunch.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelaunch.ui.theme.Background
import com.gamelaunch.ui.theme.TextHint
import com.gamelaunch.ui.theme.TextPrimary

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(48.dp)
            )
            Text("Perfil", fontSize = 18.sp, color = TextPrimary)
            Text("Próximamente", fontSize = 13.sp, color = TextHint)
        }
    }
}
