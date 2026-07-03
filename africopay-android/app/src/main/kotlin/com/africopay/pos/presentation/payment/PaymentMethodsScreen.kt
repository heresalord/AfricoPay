package com.africopay.pos.presentation.payment

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.africopay.pos.domain.model.MobileMoneyProvider
import com.africopay.pos.domain.model.PaymentMethod
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*

data class PaymentMethodUi(
    val method: PaymentMethod,
    val icon: ImageVector,
    val description: String,
    val isAvailable: Boolean = true
)

/**
 * Payment methods selection screen.
 * Dynamically built — future: filter by HardwareCapabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    amountCents: Long,
    onMethodSelected: (methodKey: String) -> Unit,
    onBack: () -> Unit
) {
    // In v0.1.0: all methods are available (simulated)
    val availableMethods = listOf(
        PaymentMethodUi(PaymentMethod.NFC, Icons.Default.Contactless, "Approchez la carte"),
        PaymentMethodUi(PaymentMethod.EMV, Icons.Default.CreditCard, "Insérez la carte à puce"),
        PaymentMethodUi(PaymentMethod.MAG_STRIPE, Icons.Default.SwipeRight, "Glissez la bande magnétique"),
        PaymentMethodUi(PaymentMethod.QR_CODE, Icons.Default.QrCode, "Scanner le QR Code"),
        PaymentMethodUi(PaymentMethod.MOBILE_MONEY, Icons.Default.PhoneAndroid, "MTN · Moov · Orange · Wave"),
        PaymentMethodUi(PaymentMethod.CASH, Icons.Default.Money, "Paiement en espèces")
    )

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
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
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
