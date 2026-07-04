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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.africopay.pos.core.util.HardwareCapabilitiesDetector
import com.africopay.pos.data.local.db.dao.TransactionDao
import com.africopay.pos.domain.model.PaperStatus
import com.africopay.pos.domain.model.PrinterStatus
import com.africopay.pos.hal.interfaces.PrinterService
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

data class DashboardKpis(
    val volume: Int = 0,
    val totalAmount: Long = 0L,
    val approvedCount: Int = 0,
    val declinedCount: Int = 0
)

data class DeviceStatus(
    val networkType: String = "NONE",
    val batteryLevel: Int = -1,
    val batteryCharging: Boolean = false,
    val printerStatus: PrinterStatus = PrinterStatus.OFFLINE,
    val printerPaper: PaperStatus = PaperStatus.OK,
    val printerConfigured: Boolean = false,
    val nfcEnabled: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    transactionDao: TransactionDao,
    private val hardwareDetector: HardwareCapabilitiesDetector,
    private val printerService: PrinterService
) : ViewModel() {

    private fun startOfDayMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val kpis: StateFlow<DashboardKpis> = combine(
        transactionDao.getTodayTransactions(startOfDayMillis()),
        transactionDao.getTodayApprovedCount(startOfDayMillis()),
        transactionDao.getTodayDeclinedCount(startOfDayMillis()),
        transactionDao.getTodayTotalAmount(startOfDayMillis())
    ) { transactions, approved, declined, total ->
        DashboardKpis(
            volume = transactions.size,
            totalAmount = total,
            approvedCount = approved,
            declinedCount = declined
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardKpis())

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    init {
        refreshDeviceStatus()
    }

    fun refreshDeviceStatus() {
        viewModelScope.launch {
            val hw = hardwareDetector.detect()
            val (printerStatus, paperStatus, printerConfigured) = withContext(Dispatchers.IO) {
                Triple(printerService.getStatus(), printerService.getPaperStatus(), printerService.isAvailable())
            }
            _deviceStatus.value = DeviceStatus(
                networkType = hw.networkType,
                batteryLevel = hw.batteryLevel,
                batteryCharging = hw.batteryCharging,
                printerStatus = printerStatus,
                printerPaper = paperStatus,
                printerConfigured = printerConfigured,
                nfcEnabled = hw.hasNfc && hw.nfcEnabled
            )
        }
    }
}

/**
 * Dashboard screen — real daily KPIs from Room, real device/printer status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    viewModel: DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val kpis by viewModel.kpis.collectAsState()
    val device by viewModel.deviceStatus.collectAsState()

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
                actions = {
                    IconButton(onClick = { viewModel.refreshDeviceStatus() }) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Aujourd'hui", color = AfricoOnDarkMuted, fontSize = 12.sp, letterSpacing = 1.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f), title = "Volume",
                    value = kpis.volume.toString(), subtitle = "transactions",
                    icon = Icons.Default.Receipt, color = AfricoGreen
                )
                KpiCard(
                    modifier = Modifier.weight(1f), title = "Montant",
                    value = formatXof(kpis.totalAmount), subtitle = "total encaissé",
                    icon = Icons.Default.TrendingUp, color = AfricoGold
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    modifier = Modifier.weight(1f), title = "Approuvées",
                    value = kpis.approvedCount.toString(), subtitle = "transactions",
                    icon = Icons.Default.CheckCircle, color = AfricoSuccess
                )
                KpiCard(
                    modifier = Modifier.weight(1f), title = "Refusées",
                    value = kpis.declinedCount.toString(), subtitle = "transactions",
                    icon = Icons.Default.Cancel, color = AfricoError
                )
            }

            Divider(color = AfricoDarkElevated)

            Text("Statut de l'Appareil", color = AfricoOnDarkMuted, fontSize = 12.sp, letterSpacing = 1.sp)

            StatusCard {
                StatusRow(
                    Icons.Default.Wifi, "Réseau",
                    if (device.networkType == "NONE") "Hors ligne" else device.networkType,
                    if (device.networkType == "NONE") AfricoError else AfricoSuccess
                )
                StatusRow(
                    Icons.Default.BatteryChargingFull, "Batterie",
                    if (device.batteryLevel < 0) "Inconnue" else "${device.batteryLevel}%" + if (device.batteryCharging) " · En charge" else "",
                    AfricoSuccess
                )
                StatusRow(
                    Icons.Default.Print, "Imprimante",
                    when {
                        !device.printerConfigured -> "Non configurée"
                        device.printerStatus == PrinterStatus.READY -> "Prête"
                        device.printerStatus == PrinterStatus.OFFLINE -> "Hors ligne"
                        else -> "Erreur"
                    },
                    when {
                        !device.printerConfigured -> AfricoOnDarkMuted
                        device.printerStatus == PrinterStatus.READY -> AfricoSuccess
                        else -> AfricoError
                    }
                )
                StatusRow(
                    Icons.Default.Contactless, "NFC",
                    if (device.nfcEnabled) "Activé" else "Désactivé / indisponible",
                    if (device.nfcEnabled) AfricoSuccess else AfricoOnDarkMuted
                )
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = AfricoOnDark, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
            Text(
                value, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
