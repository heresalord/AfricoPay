package com.africopay.pos.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*

/**
 * Dashboard screen — shows daily KPIs, device status, and connectivity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = { Text("Tableau de Bord", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = AfricoOnDarkMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AfricoDarkSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Today's Summary Header
            Text("Aujourd'hui", color = AfricoOnDarkMuted, fontSize = 12.sp, letterSpacing = 1.sp)

            // KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Volume",
                    value = "4",
                    subtitle = "transactions",
                    icon = Icons.Default.Receipt,
                    color = AfricoGreen
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Montant",
                    value = formatXof(94500L),
                    subtitle = "total encaissé",
                    icon = Icons.Default.TrendingUp,
                    color = AfricoGold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Approuvées",
                    value = "3",
                    subtitle = "transactions",
                    icon = Icons.Default.CheckCircle,
                    color = AfricoSuccess
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Refusées",
                    value = "1",
                    subtitle = "transactions",
                    icon = Icons.Default.Cancel,
                    color = AfricoError
                )
            }

            Divider(color = AfricoDarkElevated)

            // Device Status
            Text("Statut de l'Appareil", color = AfricoOnDarkMuted, fontSize = 12.sp, letterSpacing = 1.sp)

            StatusCard {
                StatusRow(Icons.Default.Wifi, "Réseau", "Wi-Fi · Signal fort", AfricoSuccess)
                StatusRow(Icons.Default.BatteryChargingFull, "Batterie", "85% · En charge", AfricoSuccess)
                StatusRow(Icons.Default.Print, "Imprimante", "Prête · Papier OK", AfricoSuccess)
                StatusRow(Icons.Default.Contactless, "NFC", "Activé", AfricoSuccess)
                StatusRow(Icons.Default.SimCard, "Mode", "SIMULATION", AfricoGold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = AfricoOnDarkMuted, fontSize = 12.sp, letterSpacing = 0.5.sp)
            Text(value, color = AfricoOnDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = AfricoOnDarkMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun StatusCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
    ) {
        Column(modifier = Modifier.padding(8.dp), content = content)
    }
}

@Composable
private fun StatusRow(icon: ImageVector, label: String, value: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = AfricoOnDark, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusColor.copy(alpha = 0.15f)
        ) {
            Text(
                value, color = statusColor, fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
