package com.africopay.pos.presentation.history

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.africopay.pos.domain.model.PaymentMethod
import com.africopay.pos.domain.model.TransactionStatus
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Placeholder data class for UI display until ViewModel + Room is wired
data class TransactionUiItem(
    val id: String,
    val amount: Long,
    val method: PaymentMethod,
    val status: TransactionStatus,
    val date: Date,
    val receiptNumber: String
)

/**
 * Transaction history screen. Lists all locally stored transactions with filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }

    // Placeholder transactions (replaced by Room + ViewModel in production)
    val transactions = remember {
        listOf(
            TransactionUiItem("1", 25000L, PaymentMethod.NFC, TransactionStatus.APPROVED, Date(), "REC-001"),
            TransactionUiItem("2", 7500L, PaymentMethod.EMV, TransactionStatus.DECLINED, Date(System.currentTimeMillis() - 300000), "REC-002"),
            TransactionUiItem("3", 50000L, PaymentMethod.MOBILE_MONEY, TransactionStatus.APPROVED, Date(System.currentTimeMillis() - 600000), "REC-003"),
            TransactionUiItem("4", 12000L, PaymentMethod.CASH, TransactionStatus.APPROVED, Date(System.currentTimeMillis() - 900000), "REC-004"),
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    val filtered = transactions.filter {
        searchQuery.isEmpty() ||
            it.receiptNumber.contains(searchQuery, ignoreCase = true) ||
            it.amount.toString().contains(searchQuery)
    }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = { Text("Historique", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = AfricoOnDarkMuted)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Export */ }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exporter", tint = AfricoGreen)
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher...", color = AfricoOnDarkMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AfricoOnDarkMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AfricoGreen,
                    unfocusedBorderColor = AfricoDarkElevated,
                    focusedTextColor = AfricoOnDark,
                    unfocusedTextColor = AfricoOnDark,
                    cursorColor = AfricoGreen
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null,
                            tint = AfricoOnDarkMuted, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Aucune transaction", color = AfricoOnDarkMuted, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { txn ->
                        TransactionCard(txn = txn, dateFormat = dateFormat)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(txn: TransactionUiItem, dateFormat: SimpleDateFormat) {
    val statusColor = when (txn.status) {
        TransactionStatus.APPROVED -> AfricoSuccess
        TransactionStatus.CANCELLED -> AfricoOnDarkMuted
        else -> AfricoError
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (txn.status == TransactionStatus.APPROVED) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.method.displayName, color = AfricoOnDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(dateFormat.format(txn.date), color = AfricoOnDarkMuted, fontSize = 12.sp)
                Text(txn.receiptNumber, color = AfricoOnDarkMuted, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatXof(txn.amount), color = AfricoOnDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = when (txn.status) {
                        TransactionStatus.APPROVED -> "Approuvé"
                        TransactionStatus.DECLINED -> "Refusé"
                        TransactionStatus.CANCELLED -> "Annulé"
                        else -> txn.status.name
                    },
                    color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
