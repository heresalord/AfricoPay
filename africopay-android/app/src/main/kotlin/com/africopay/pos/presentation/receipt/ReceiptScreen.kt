package com.africopay.pos.presentation.receipt

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.africopay.pos.data.local.db.dao.ReceiptDao
import com.africopay.pos.data.local.db.entity.ReceiptEntity
import com.africopay.pos.domain.model.PrintResult
import com.africopay.pos.hal.interfaces.PrinterService
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class PrintUiState {
    object Idle : PrintUiState()
    object Printing : PrintUiState()
    data class Done(val result: PrintResult) : PrintUiState()
}

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val printerService: PrinterService
) : ViewModel() {

    private val _receipt = MutableStateFlow<ReceiptEntity?>(null)
    val receipt: StateFlow<ReceiptEntity?> = _receipt.asStateFlow()

    private val _printState = MutableStateFlow<PrintUiState>(PrintUiState.Idle)
    val printState: StateFlow<PrintUiState> = _printState.asStateFlow()

    private val _printerAvailable = MutableStateFlow(false)
    val printerAvailable: StateFlow<Boolean> = _printerAvailable.asStateFlow()

    fun load(transactionId: String) {
        viewModelScope.launch {
            _receipt.value = receiptDao.getReceiptByTransactionId(transactionId)
            _printerAvailable.value = withContext(kotlinx.coroutines.Dispatchers.IO) { printerService.isAvailable() }
        }
    }

    fun print() {
        val current = _receipt.value ?: return
        viewModelScope.launch {
            _printState.value = PrintUiState.Printing
            val domainReceipt = com.africopay.pos.domain.model.Receipt(
                id = current.id,
                transactionId = current.transactionId,
                receiptNumber = current.receiptNumber,
                merchantName = current.merchantName,
                merchantAddress = current.merchantAddress,
                merchantPhone = current.merchantPhone,
                merchantId = current.merchantId,
                terminalId = current.terminalId,
                paymentMethod = com.africopay.pos.domain.model.PaymentMethod.valueOf(current.paymentMethod),
                amount = current.amount,
                currency = current.currency,
                status = com.africopay.pos.domain.model.TransactionStatus.valueOf(current.status),
                dateTime = java.time.Instant.ofEpochMilli(current.dateTime),
                footerText = current.footerText,
                isSimulated = current.isSimulated
            )
            val result = withContext(kotlinx.coroutines.Dispatchers.IO) { printerService.print(domainReceipt) }
            if (result == PrintResult.SUCCESS) {
                receiptDao.incrementPrintCount(current.id, System.currentTimeMillis())
            }
            _printState.value = PrintUiState.Done(result)
        }
    }
}

/**
 * Receipt screen — renders the real stored receipt for [transactionId] and prints it
 * for real via the configured Bluetooth printer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    transactionId: String,
    onDone: () -> Unit,
    onViewHistory: () -> Unit,
    viewModel: ReceiptViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val receipt by viewModel.receipt.collectAsState()
    val printState by viewModel.printState.collectAsState()
    val printerAvailable by viewModel.printerAvailable.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.FRANCE) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(transactionId) { viewModel.load(transactionId) }

    LaunchedEffect(printState) {
        when (val s = printState) {
            is PrintUiState.Done -> {
                val message = when (s.result) {
                    PrintResult.SUCCESS -> "Reçu imprimé"
                    PrintResult.PAPER_EMPTY -> "Papier épuisé"
                    PrintResult.TIMEOUT -> "Délai d'impression dépassé"
                    PrintResult.PRINTER_ERROR -> "Aucune imprimante configurée ou accessible — vérifiez Paramètres > Imprimante"
                }
                snackbarHostState.showSnackbar(message)
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = AfricoDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reçu", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AfricoDarkSurface)
            )
        }
    ) { padding ->
        val current = receipt
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AfricoGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF7))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (current.isSimulated) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = AfricoGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "★ TRANSACTION TEST ★",
                                color = AfricoGoldDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(current.merchantName, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = AfricoDark, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                    current.merchantAddress?.let {
                        Text(it, fontSize = 13.sp, color = AfricoDarkElevated, fontFamily = FontFamily.Monospace)
                    }
                    current.merchantPhone?.let {
                        Text(it, fontSize = 11.sp, color = AfricoDarkElevated, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptRow("Date", dateFormat.format(Date(current.dateTime)))
                    ReceiptRow("Heure", timeFormat.format(Date(current.dateTime)))
                    ReceiptRow("N° Reçu", current.receiptNumber)
                    ReceiptRow("Terminal", current.terminalId)
                    ReceiptRow("Marchand ID", current.merchantId)
                    ReceiptRow("Méthode", com.africopay.pos.domain.model.PaymentMethod.valueOf(current.paymentMethod).displayName)
                    ReceiptRow("Référence", current.transactionId.take(16).uppercase())

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("MONTANT", fontSize = 11.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    Text(
                        formatXof(current.amount),
                        fontSize = 32.sp, fontWeight = FontWeight.Bold,
                        color = AfricoDark, fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    val isApproved = current.status == "APPROVED"
                    Text(
                        if (isApproved) "✓ APPROUVÉ" else "✗ ${statusLabelFr(current.status)}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (isApproved) AfricoSuccess else AfricoError,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    current.footerText?.let {
                        Text(it, fontSize = 13.sp, color = AfricoDark,
                            fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                    }
                    Text("www.africopay.com", fontSize = 11.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!printerAvailable) {
                Text(
                    "Aucune imprimante Bluetooth associée — allez dans Paramètres pour en choisir une",
                    color = AfricoOnDarkMuted, fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.print() },
                enabled = printState !is PrintUiState.Printing,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AfricoGreen)
            ) {
                if (printState is PrintUiState.Printing) {
                    CircularProgressIndicator(color = AfricoDark, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Print, contentDescription = null, tint = AfricoDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("IMPRIMER LE REÇU", fontWeight = FontWeight.Bold, color = AfricoDark)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = AfricoGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Historique", color = AfricoGreen)
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AfricoDarkSurface)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = AfricoOnDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accueil", color = AfricoOnDark)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = AfricoDarkElevated, fontFamily = FontFamily.Monospace)
        Text(value, fontSize = 11.sp, color = AfricoDark, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DashedDivider() {
    Text(
        text = "- ".repeat(24),
        fontSize = 10.sp, color = AfricoDarkElevated,
        fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun statusLabelFr(status: String): String = when (status) {
    "DECLINED" -> "REFUSÉ"
    "TIMEOUT" -> "DÉLAI DÉPASSÉ"
    "NETWORK_ERROR" -> "ERREUR RÉSEAU"
    "CANCELLED" -> "ANNULÉ"
    "CARD_EXPIRED" -> "CARTE EXPIRÉE"
    "INSUFFICIENT_FUNDS" -> "FONDS INSUFFISANTS"
    "ISSUER_OFFLINE" -> "ÉMETTEUR HORS LIGNE"
    else -> "ERREUR"
}
