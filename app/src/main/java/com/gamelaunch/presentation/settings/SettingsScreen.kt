package com.gamelaunch.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.domain.model.Region

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
            // Notificaciones
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

            // Región
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Región de lanzamientos", style = MaterialTheme.typography.titleMedium)
                Text(
                    "El calendario mostrará lanzamientos de la región seleccionada por defecto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RegionSelector(
                    selected = state.preferredRegion,
                    onSelect = viewModel::setPreferredRegion
                )
            }
        }
    }
}

@Composable
private fun RegionSelector(
    selected: Region,
    onSelect: (Region) -> Unit
) {
    val regions = listOf(
        Region.WORLDWIDE,
        Region.EUROPE,
        Region.NORTH_AMERICA,
        Region.JAPAN,
        Region.ASIA,
        Region.AUSTRALIA,
        Region.BRAZIL,
        Region.KOREA
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        regions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { region ->
                    FilterChip(
                        selected = selected == region,
                        onClick = { onSelect(region) },
                        label = { Text(region.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
