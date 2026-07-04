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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.africopay.pos.data.local.db.dao.TransactionDao
import com.africopay.pos.data.local.db.entity.TransactionEntity
import com.africopay.pos.domain.model.PaymentMethod
import com.africopay.pos.presentation.home.formatXof
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    transactionDao: TransactionDao
) : ViewModel() {
    val transactions: StateFlow<List<TransactionEntity>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

/**
 * Transaction history screen. Lists all locally stored transactions with filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val transactions by viewModel.transactions.collectAsState()

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
                        Text(
                            if (transactions.isEmpty()) "Aucune transaction" else "Aucun résultat",
                            color = AfricoOnDarkMuted, fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { txn ->
                        TransactionCard(txn = txn, dateFormat = dateFormat)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(txn: TransactionEntity, dateFormat: SimpleDateFormat) {
    val isApproved = txn.status == "APPROVED"
    val statusColor = when (txn.status) {
        "APPROVED" -> AfricoSuccess
        "CANCELLED" -> AfricoOnDarkMuted
        else -> AfricoError
    }
    val method = try { PaymentMethod.valueOf(txn.paymentMethod).displayName } catch (e: Exception) { txn.paymentMethod }

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
                        imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(method, color = AfricoOnDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(dateFormat.format(Date(txn.timestamp)), color = AfricoOnDarkMuted, fontSize = 12.sp)
                Text(txn.receiptNumber, color = AfricoOnDarkMuted, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatXof(txn.amount), color = AfricoOnDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = when (txn.status) {
                        "APPROVED" -> "Approuvé"
                        "DECLINED" -> "Refusé"
                        "CANCELLED" -> "Annulé"
                        "TIMEOUT" -> "Délai dépassé"
                        else -> txn.status
                    },
                    color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
