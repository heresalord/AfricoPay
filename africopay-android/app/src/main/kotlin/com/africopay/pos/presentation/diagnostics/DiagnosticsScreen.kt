package com.africopay.pos.presentation.diagnostics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.africopay.pos.presentation.theme.*

data class DiagnosticItem(
    val name: String,
    val isAvailable: Boolean,
    val icon: ImageVector,
    val version: String? = null,
    val details: String? = null
)

/**
 * Hardware diagnostics screen — shows availability and version of all POS peripherals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {

    // Simulated diagnostic items — in production, populated from HardwareCapabilities
    val components = remember {
        listOf(
            DiagnosticItem("NFC", true, Icons.Default.Contactless, details = "Activé"),
            DiagnosticItem("Lecteur EMV", true, Icons.Default.CreditCard, version = "2.1.0"),
            DiagnosticItem("Bande Magnétique", true, Icons.Default.SwipeRight, version = "1.0.0"),
            DiagnosticItem("Imprimante Thermique", true, Icons.Default.Print, version = "3.2.1", details = "Papier OK"),
            DiagnosticItem("Caméra", true, Icons.Default.PhotoCamera),
            DiagnosticItem("Scanner Code-barres", false, Icons.Default.QrCodeScanner, details = "Non disponible"),
            DiagnosticItem("GPS", true, Icons.Default.LocationOn),
            DiagnosticItem("Bluetooth", true, Icons.Default.Bluetooth, details = "Désactivé"),
            DiagnosticItem("Wi-Fi", true, Icons.Default.Wifi, details = "Connecté"),
            DiagnosticItem("USB", true, Icons.Default.Usb),
        )
    }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = { Text("Diagnostic Matériel", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = AfricoOnDarkMuted)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Re-run diagnostics */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = AfricoGreen)
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Device Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = AfricoGreen, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AfricoPay Terminal", color = AfricoOnDark, fontWeight = FontWeight.Bold)
                        Text("Fabricant: SIMULATION · Android 13", color = AfricoOnDarkMuted, fontSize = 12.sp)
                        Text("Mode: Simulation v0.1.0", color = AfricoGold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Composants", color = AfricoOnDarkMuted, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(components) { item ->
                    DiagnosticCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticCard(item: DiagnosticItem) {
    val statusColor = if (item.isAvailable) AfricoSuccess else AfricoError
    val statusText = if (item.isAvailable) "DISPONIBLE" else "INDISPONIBLE"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = AfricoOnDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (item.version != null) {
                    Text("v${item.version}", color = AfricoOnDarkMuted, fontSize = 11.sp)
                }
                if (item.details != null) {
                    Text(item.details, color = AfricoOnDarkMuted, fontSize = 11.sp)
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    statusText, color = statusColor, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
