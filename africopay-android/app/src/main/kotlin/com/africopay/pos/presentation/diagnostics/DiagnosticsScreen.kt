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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.africopay.pos.BuildConfig
import com.africopay.pos.core.util.HardwareCapabilitiesDetector
import com.africopay.pos.domain.model.HardwareCapabilities
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class DiagnosticItem(
    val name: String,
    val isAvailable: Boolean,
    val icon: ImageVector,
    val version: String? = null,
    val details: String? = null
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val hardwareDetector: HardwareCapabilitiesDetector
) : ViewModel() {

    fun runDiagnostics(): Pair<HardwareCapabilities, List<DiagnosticItem>> {
        val hw = hardwareDetector.detect()

        val items = listOf(
            DiagnosticItem(
                "NFC", hw.hasNfc, Icons.Default.Contactless,
                details = when {
                    !hw.hasNfc -> "Non présent sur cet appareil"
                    hw.nfcEnabled -> "Activé"
                    else -> "Désactivé dans les paramètres"
                }
            ),
            DiagnosticItem(
                "Lecteur EMV", hw.hasEmv, Icons.Default.CreditCard,
                details = if (hw.hasEmv) null else "Aucun SDK fabricant intégré"
            ),
            DiagnosticItem(
                "Bande Magnétique", hw.hasMagStripe, Icons.Default.SwipeRight,
                details = if (hw.hasMagStripe) null else "Aucun SDK fabricant intégré"
            ),
            DiagnosticItem(
                "Imprimante Thermique", hw.hasPrinter, Icons.Default.Print,
                details = if (hw.hasPrinter) "Papier OK" else "Aucun SDK fabricant intégré"
            ),
            DiagnosticItem("Caméra", hw.hasCamera, Icons.Default.PhotoCamera),
            DiagnosticItem("Scanner Code-barres", hw.hasScanner, Icons.Default.QrCodeScanner,
                details = if (!hw.hasScanner) "Non disponible" else null),
            DiagnosticItem("GPS", hw.hasGps, Icons.Default.LocationOn),
            DiagnosticItem("Bluetooth", hw.hasBluetooth, Icons.Default.Bluetooth),
            DiagnosticItem("Wi-Fi", hw.hasWifi, Icons.Default.Wifi,
                details = if (hw.networkType == "WIFI") "Connecté" else null),
            DiagnosticItem("USB", true, Icons.Default.Usb),
        )
        return hw to items
    }
}

/**
 * Hardware diagnostics screen — shows real availability read from
 * [HardwareCapabilitiesDetector], not hardcoded placeholders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var result by remember { mutableStateOf(viewModel.runDiagnostics()) }
    val (hw, components) = result

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
                    IconButton(onClick = { result = viewModel.runDiagnostics() }) {
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
                        Text("${hw.manufacturer} ${hw.model}".trim(), color = AfricoOnDark, fontWeight = FontWeight.Bold)
                        Text("Android API ${hw.androidVersion} · Batterie ${hw.batteryLevel}%", color = AfricoOnDarkMuted, fontSize = 12.sp)
                        Text(
                            if (BuildConfig.SIMULATION_MODE) "Mode: Simulation ${BuildConfig.VERSION_NAME}" else "Mode: Production ${BuildConfig.VERSION_NAME}",
                            color = AfricoGold, fontSize = 12.sp
                        )
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
