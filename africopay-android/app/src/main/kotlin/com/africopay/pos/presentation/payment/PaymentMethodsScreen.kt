package com.africopay.pos.presentation.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.ViewModel
import com.africopay.pos.core.util.HardwareCapabilitiesDetector
import com.africopay.pos.domain.model.HardwareCapabilities
import com.africopay.pos.domain.model.PaymentMethod
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class PaymentMethodUi(
    val method: PaymentMethod,
    val icon: ImageVector,
    val description: String,
    val isAvailable: Boolean,
    val unavailableReason: String? = null
)

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val hardwareDetector: HardwareCapabilitiesDetector
) : ViewModel() {

    /** Builds the method list from real device capabilities every time the screen is shown. */
    fun buildAvailableMethods(): List<PaymentMethodUi> {
        val hw: HardwareCapabilities = hardwareDetector.detect()

        return listOfNotNull(
            // NFC: hidden entirely if the device has no NFC radio at all.
            if (hw.hasNfc) {
                PaymentMethodUi(
                    method = PaymentMethod.NFC,
                    icon = Icons.Default.Contactless,
                    description = if (hw.nfcEnabled) "Approchez la carte" else "NFC désactivé — activez-le dans les paramètres",
                    isAvailable = hw.nfcEnabled,
                    unavailableReason = if (!hw.nfcEnabled) "NFC désactivé" else null
                )
            } else null,

            // EMV / Mag stripe require a certified Smart POS reader (ZCS/Sunmi/PAX/...).
            // No manufacturer SDK is integrated yet, so these are hidden until one is —
            // same rule as NFC, no simulated fallback.
            if (hw.hasEmv) {
                PaymentMethodUi(
                    method = PaymentMethod.EMV,
                    icon = Icons.Default.CreditCard,
                    description = "Insérez la carte à puce",
                    isAvailable = true
                )
            } else null,

            if (hw.hasMagStripe) {
                PaymentMethodUi(
                    method = PaymentMethod.MAG_STRIPE,
                    icon = Icons.Default.SwipeRight,
                    description = "Glissez la bande magnétique",
                    isAvailable = true
                )
            } else null,

            // QR generation has no hardware dependency — always available.
            PaymentMethodUi(
                method = PaymentMethod.QR_CODE,
                icon = Icons.Default.QrCode,
                description = "Scanner le QR Code",
                isAvailable = true
            ),

            // Mobile Money / Cash have no hardware dependency — always available.
            PaymentMethodUi(
                method = PaymentMethod.MOBILE_MONEY,
                icon = Icons.Default.PhoneAndroid,
                description = "MTN · Moov · Orange · Wave",
                isAvailable = true
            ),
            PaymentMethodUi(
                method = PaymentMethod.CASH,
                icon = Icons.Default.Money,
                description = "Paiement en espèces",
                isAvailable = true
            )
        )
    }
}

/**
 * Payment methods selection screen. The list reflects real device hardware:
 * NFC only appears if the device has an NFC radio (and shows a hint if it's disabled).
 * EMV / mag-stripe only appear when real reader hardware is detected, or in simulation mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    amountCents: Long,
    onMethodSelected: (methodKey: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentMethodsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val availableMethods = remember { viewModel.buildAvailableMethods() }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Choisir le paiement", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AfricoOnDark)
                        Text(formatXof(amountCents), fontSize = 14.sp, color = AfricoGreen, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = AfricoOnDarkMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AfricoDarkSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (availableMethods.none { it.method == PaymentMethod.NFC }) {
                item {
                    Text(
                        "NFC non disponible sur cet appareil",
                        color = AfricoOnDarkMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            items(availableMethods) { item ->
                PaymentMethodCard(
                    item = item,
                    onClick = { onMethodSelected(item.method.name) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PaymentMethodCard(item: PaymentMethodUi, onClick: () -> Unit) {
    val contentAlpha = if (item.isAvailable) 1f else 0.5f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = item.isAvailable, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AfricoDarkSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp).alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AfricoGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = AfricoGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.method.displayName,
                    color = AfricoOnDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.description,
                    color = AfricoOnDarkMuted,
                    fontSize = 13.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AfricoOnDarkMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
