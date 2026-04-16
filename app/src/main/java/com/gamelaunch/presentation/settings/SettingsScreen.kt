package com.gamelaunch.presentation.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.domain.model.Platform
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Notificaciones ────────────────────────────────────────────
            NotificationPermissionHandler()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notificarme con antelación", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 7).forEach { days ->
                        FilterChip(
                            selected = state.notifyDaysAhead == days,
                            onClick = { viewModel.setNotifyDaysAhead(days) },
                            label = { Text("$days día${if (days > 1) "s" else ""}") }
                        )
                    }
                }
            }

            HorizontalDivider()

            // ── Plataformas favoritas ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Plataformas favoritas", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Filtra el calendario por tus plataformas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FavoritePlatformsSelector(
                    selected = state.favoritePlatforms,
                    onToggle = viewModel::toggleFavoritePlatform
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionHandler() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val notifPermission = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        if (!notifPermission.status.isGranted) {
            notifPermission.launchPermissionRequest()
        }
    }
}

@Composable
private fun FavoritePlatformsSelector(
    selected: Set<Platform>,
    onToggle: (Platform) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Platform.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { platform ->
                    FilterChip(
                        selected = platform in selected,
                        onClick  = { onToggle(platform) },
                        label    = { Text(platform.displayName) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(platform.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
