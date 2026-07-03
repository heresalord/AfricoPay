package com.africopay.pos.presentation.receipt

import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Receipt screen — shows a styled thermal-receipt simulation.
 * In simulation mode, always shows "TRANSACTION TEST" banner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    transactionId: String,
    onDone: () -> Unit,
    onViewHistory: () -> Unit
) {
    val now = remember { Date() }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.FRANCE) }
    val receiptNumber = remember { "REC-${SimpleDateFormat("yyyyMMdd", Locale.FRANCE).format(now)}-001" }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = { Text("Reçu", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AfricoDarkSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thermal receipt simulation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFAFAF7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TEST SIMULATION Banner
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

                    // Merchant Header
                    Text(
                        "AFRICOPAY POS",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = AfricoDark, fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Boutique Demo",
                        fontSize = 13.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "Cotonou, Bénin · +229 00 00 00 00",
                        fontSize = 11.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Transaction Details
                    ReceiptRow("Date", dateFormat.format(now))
                    ReceiptRow("Heure", timeFormat.format(now))
                    ReceiptRow("N° Reçu", receiptNumber)
                    ReceiptRow("Terminal", "SIM-001")
                    ReceiptRow("Marchand ID", "MERCH-12345")
                    ReceiptRow("Méthode", "Carte Sans Contact")
                    ReceiptRow("Référence", transactionId.take(16).uppercase())

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount
                    Text("MONTANT", fontSize = 11.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    Text(
                        formatXof(15000L),
                        fontSize = 32.sp, fontWeight = FontWeight.Bold,
                        color = AfricoDark, fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Status
                    Text(
                        "✓ APPROUVÉ",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = AfricoSuccess, fontFamily = FontFamily.Monospace
                    )
                    Text("AUTH 847291", fontSize = 12.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace)

                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Merci pour votre achat !",
                        fontSize = 13.sp, color = AfricoDark,
                        fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center
                    )
                    Text(
                        "www.africopay.com",
                        fontSize = 11.sp, color = AfricoDarkElevated,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Print button
            Button(
                onClick = { /* TODO: PrintReceiptUseCase */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AfricoGreen)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, tint = AfricoDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("IMPRIMER LE REÇU", fontWeight = FontWeight.Bold, color = AfricoDark)
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
