package com.gamelaunch.presentation.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.BuildConfig
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.ui.components.PlatformIcon
import com.gamelaunch.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationPermissionHandler()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryFixed
                        )
                    }
                },
                title = {
                    Text(
                        text = "AJUSTES",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = (-0.5).sp,
                        color = PrimaryFixed
                    )
                },
                actions = {
                    // Balancing spacer to keep title centered
                    Spacer(Modifier.size(48.dp))
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item { IdiomaSection(uiState = uiState, onSetLanguage = viewModel::setLanguage) }
            item { NotificacionesSection(uiState = uiState, onSetDays = viewModel::setNotifyDaysAhead) }
            item { PlataformasSection(uiState = uiState, onToggle = viewModel::toggleFavoritePlatform) }
            item { AcercaDeSection() }
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        fontFamily = ManropeFamily,
        color = PrimaryFixed,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

// ── Idioma ────────────────────────────────────────────────────────────────────

@Composable
private fun IdiomaSection(
    uiState: SettingsUiState,
    onSetLanguage: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("IDIOMA")

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isSpanish = uiState.language == "es"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSpanish) PrimaryFixed else Color.Transparent)
                        .clickable { onSetLanguage("es") }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Español",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        fontWeight = if (isSpanish) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isSpanish) OnPrimary else OnSurfaceVariant
                    )
                }

                val isEnglish = uiState.language == "en"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isEnglish) PrimaryFixed else Color.Transparent)
                        .clickable { onSetLanguage("en") }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "English",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        fontWeight = if (isEnglish) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isEnglish) OnPrimary else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Notificaciones ────────────────────────────────────────────────────────────

@Composable
private fun NotificacionesSection(
    uiState: SettingsUiState,
    onSetDays: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("NOTIFICACIONES")

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // Fila — aviso anticipado con selector de días
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SurfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = PrimaryFixed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Notificaciones de lanzamiento",
                                fontFamily = ManropeFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Avisos de nuevos estrenos",
                                fontFamily = ManropeFamily,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = SurfaceContainerHighest.copy(alpha = 0.20f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // Fila — anticipación con selector 1/3/7 días
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SurfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Recordatorios anticipados",
                                fontFamily = ManropeFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                            Text(
                                text = "Aviso antes del lanzamiento",
                                fontFamily = ManropeFamily,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Selector de días
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 7).forEach { days ->
                        val isSelected = uiState.notifyDaysAhead == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (isSelected) PrimaryFixed else SurfaceContainerHigh
                                )
                                .clickable { onSetDays(days) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$days día${if (days > 1) "s" else ""}",
                                fontFamily = ManropeFamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) OnPrimary else OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Plataformas favoritas ────────────────────────────────────────────────────

@Composable
private fun PlataformasSection(
    uiState: SettingsUiState,
    onToggle: (Platform) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("PLATAFORMAS FAVORITAS")

        Platform.entries.chunked(2).forEach { rowPlatforms ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowPlatforms.forEach { platform ->
                    val isActive = platform in uiState.favoritePlatforms
                    Card(
                        onClick = { onToggle(platform) },
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = if (isActive) PrimaryFixed else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            PlatformIcon(
                                platform = platform,
                                size = 36.dp,
                                tint = if (isActive) PrimaryFixed else OnSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = platform.displayName,
                                fontFamily = ManropeFamily,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                                color = if (isActive) PrimaryFixed else OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (rowPlatforms.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Acerca de ─────────────────────────────────────────────────────────────────

@Composable
private fun AcercaDeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("ACERCA DE")

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Versión",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryFixed,
                        modifier = Modifier
                            .background(
                                PrimaryFixed.copy(alpha = 0.10f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                HorizontalDivider(
                    color = SurfaceContainerHighest.copy(alpha = 0.20f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Política de privacidad",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(
                    color = SurfaceContainerHighest.copy(alpha = 0.20f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Términos de uso",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Notification permission handler ──────────────────────────────────────────

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
