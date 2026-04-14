package com.gamelaunch.presentation.profile

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.R
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.presentation.settings.SettingsViewModel
import com.gamelaunch.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Ajustes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Personaliza tu experiencia",
                fontSize = 13.sp,
                color = TextHint
            )
        }

        // ── Sección: Notificaciones ────────────────────────────────────────────
        SettingsSection(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.settings_notifications),
            subtitle = "Avísame antes de cada lanzamiento"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listOf(1, 3, 7).forEach { days ->
                    val selected = state.notifyDaysAhead == days
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setNotifyDaysAhead(days) },
                        label = {
                            Text(
                                text = "$days día${if (days > 1) "s" else ""}",
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentDim,
                            selectedLabelColor = Accent,
                            containerColor = SurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = Accent,
                            borderColor = BorderSubtle,
                            selectedBorderWidth = 1.dp,
                            borderWidth = 0.5.dp
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Sección: Plataformas favoritas ────────────────────────────────────
        SettingsSection(
            icon = Icons.Default.Star,
            title = "Plataformas favoritas",
            subtitle = "Filtra el calendario por tus plataformas"
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Platform.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { platform ->
                            val isSelected = platform in state.favoritePlatforms
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleFavoritePlatform(platform) },
                                label = { Text(platform.displayName, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(platform.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentDim,
                                    selectedLabelColor = Accent,
                                    containerColor = SurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = Accent,
                                    borderColor = BorderSubtle,
                                    selectedBorderWidth = 1.dp,
                                    borderWidth = 0.5.dp
                                )
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Sección: Idioma ────────────────────────────────────────────────────
        SettingsSection(
            icon = Icons.Default.Language,
            title = stringResource(R.string.language),
            subtitle = "Cambia el idioma de la app"
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                listOf(
                    "es" to stringResource(R.string.language_es),
                    "en" to stringResource(R.string.language_en)
                ).forEach { (code, name) ->
                    val isSelected = state.language == code
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setLanguage(code) { activity.recreate() }
                        },
                        label = { Text(name, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentDim,
                            selectedLabelColor = Accent,
                            containerColor = SurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Accent,
                            borderColor = BorderSubtle,
                            selectedBorderWidth = 1.dp,
                            borderWidth = 0.5.dp
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Surface, RoundedCornerShape(12.dp))
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(AccentDim, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextHint
                )
            }
        }
        content()
    }
}
