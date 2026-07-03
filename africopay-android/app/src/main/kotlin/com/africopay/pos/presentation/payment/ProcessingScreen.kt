package com.africopay.pos.presentation.payment

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.africopay.pos.domain.model.*
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ─── ViewModel ────────────────────────────────────────────────────────────────

sealed class ProcessingState {
    object Waiting   : ProcessingState()
    object Processing: ProcessingState()
    data class Result(val paymentResult: PaymentResult, val method: PaymentMethod) : ProcessingState()
}

@HiltViewModel
class ProcessingViewModel @Inject constructor(
    private val simulationConfig: SimulationConfig
) : ViewModel() {

    private val _state = MutableStateFlow<ProcessingState>(ProcessingState.Waiting)
    val state: StateFlow<ProcessingState> = _state.asStateFlow()

    fun processPayment(amountCents: Long, method: PaymentMethod) {
        viewModelScope.launch {
            _state.value = ProcessingState.Processing
            delay(simulationConfig.processingDelayMs)
            val status = simulationConfig.nextResult(amountCents)
            val result = PaymentResult(
                status = status,
                transactionId = UUID.randomUUID().toString(),
                receiptNumber = "SIM-${method.name}-${System.currentTimeMillis()}",
                authCode = if (status == TransactionStatus.APPROVED) "AUTH${(100000..999999).random()}" else null
            )
            _state.value = ProcessingState.Result(result, method)
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    amountCents: Long,
    paymentMethodKey: String,
    onTransactionComplete: (transactionId: String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ProcessingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val method = remember { PaymentMethod.valueOf(paymentMethodKey) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.processPayment(amountCents, method)
    }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = { Text(method.displayName, color = AfricoOnDark, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AfricoDarkSurface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is ProcessingState.Waiting,
                is ProcessingState.Processing -> ProcessingIndicator(method = method, amountCents = amountCents)
                is ProcessingState.Result     -> {
                    ResultView(
                        result = s.paymentResult,
                        method = s.method,
                        amountCents = amountCents,
                        onContinue = { onTransactionComplete(s.paymentResult.transactionId) },
                        onRetry = { viewModel.processPayment(amountCents, method) },
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingIndicator(method: PaymentMethod, amountCents: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = AfricoGreen.copy(alpha = 0.15f),
            modifier = Modifier.scale(scale).size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = methodIcon(method),
                    contentDescription = null,
                    tint = AfricoGreen,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Traitement en cours...", color = AfricoOnDark, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(formatXof(amountCents), color = AfricoGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = AfricoGreen, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(instructionText(method), color = AfricoOnDarkMuted, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
private fun ResultView(
    result: PaymentResult,
    method: PaymentMethod,
    amountCents: Long,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val isApproved = result.status == TransactionStatus.APPROVED
    val statusColor = if (isApproved) AfricoSuccess else AfricoError
    val statusIcon = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel
    val statusText = if (isApproved) "APPROUVÉ" else statusLabel(result.status)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = statusColor.copy(alpha = 0.15f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(60.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(statusText, color = statusColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(formatXof(amountCents), color = AfricoOnDark, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(method.displayName, color = AfricoOnDarkMuted, fontSize = 14.sp)
        if (result.authCode != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Code d'autorisation: ${result.authCode}",
                    color = AfricoGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isApproved) AfricoGreen else AfricoError)
        ) {
            Text(if (isApproved) "VOIR LE REÇU" else "FERMER", fontWeight = FontWeight.Bold,
                color = if (isApproved) Color.Black else Color.White)
        }
        if (!isApproved) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("RÉESSAYER", color = AfricoGreen, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCancel) {
            Text("Accueil", color = AfricoOnDarkMuted)
        }
    }
}

private fun methodIcon(method: PaymentMethod): ImageVector = when (method) {
    PaymentMethod.NFC        -> Icons.Default.Contactless
    PaymentMethod.EMV        -> Icons.Default.CreditCard
    PaymentMethod.MAG_STRIPE -> Icons.Default.SwipeRight
    PaymentMethod.QR_CODE    -> Icons.Default.QrCode
    PaymentMethod.MOBILE_MONEY -> Icons.Default.PhoneAndroid
    PaymentMethod.CASH       -> Icons.Default.Money
}

private fun instructionText(method: PaymentMethod): String = when (method) {
    PaymentMethod.NFC        -> "Approchez la carte du terminal"
    PaymentMethod.EMV        -> "Maintenez la carte insérée"
    PaymentMethod.MAG_STRIPE -> "Glissez la carte"
    PaymentMethod.QR_CODE    -> "Scan du code en cours..."
    PaymentMethod.MOBILE_MONEY -> "En attente de confirmation..."
    PaymentMethod.CASH       -> "Enregistrement du paiement..."
}

private fun statusLabel(status: TransactionStatus): String = when (status) {
    TransactionStatus.APPROVED          -> "APPROUVÉ"
    TransactionStatus.DECLINED          -> "REFUSÉ"
    TransactionStatus.TIMEOUT           -> "DÉLAI DÉPASSÉ"
    TransactionStatus.NETWORK_ERROR     -> "ERREUR RÉSEAU"
    TransactionStatus.CANCELLED         -> "ANNULÉ"
    TransactionStatus.CARD_EXPIRED      -> "CARTE EXPIRÉE"
    TransactionStatus.INSUFFICIENT_FUNDS -> "FONDS INSUFFISANTS"
    TransactionStatus.ISSUER_OFFLINE    -> "ÉMETTEUR HORS LIGNE"
    TransactionStatus.UNKNOWN_ERROR     -> "ERREUR INCONNUE"
}
