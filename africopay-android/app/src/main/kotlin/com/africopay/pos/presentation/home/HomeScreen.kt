package com.africopay.pos.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.africopay.pos.presentation.theme.*

/**
 * AfricoPay Home Screen — Numeric keypad for amount entry.
 * Primary language: French.
 */
@Composable
fun HomeScreen(
    onContinue: (amountCents: Long) -> Unit,
    onHistoryClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    val maxDigits = 9

    val displayAmount = remember(amountStr) {
        if (amountStr.isEmpty()) "0"
        else {
            val cents = amountStr.toLongOrNull() ?: 0L
            formatXof(cents)
        }
    }

    fun appendDigit(digit: String) {
        if (amountStr.length < maxDigits) {
            amountStr += digit
        }
    }

    fun deleteDigit() {
        if (amountStr.isNotEmpty()) {
            amountStr = amountStr.dropLast(1)
        }
    }

    fun onContinuePressed() {
        val cents = amountStr.toLongOrNull() ?: 0L
        if (cents > 0) onContinue(cents)
    }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            AfricoTopBar(
                onDashboardClick = onDashboardClick,
                onHistoryClick = onHistoryClick,
                onDiagnosticsClick = onDiagnosticsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Amount Display
            AmountDisplay(displayAmount = displayAmount, currency = "XOF")

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            NumericKeypad(
                onDigit = ::appendDigit,
                onDelete = ::deleteDigit,
                onClear = { amountStr = "" }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            val hasAmount = (amountStr.toLongOrNull() ?: 0L) > 0
            Button(
                onClick = ::onContinuePressed,
                enabled = hasAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AfricoGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = AfricoDarkElevated,
                    disabledContentColor = AfricoOnDarkMuted
                )
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONTINUER", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            // Cancel Button
            TextButton(
                onClick = { amountStr = "" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 4.dp)
            ) {
                Text("ANNULER", color = AfricoError, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AfricoTopBar(
    onDashboardClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "AfricoPay",
                    color = AfricoGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "MODE SIMULATION",
                    color = AfricoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onDashboardClick) {
                Icon(Icons.Default.BarChart, contentDescription = "Tableau de bord", tint = AfricoOnDarkMuted)
            }
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Default.History, contentDescription = "Historique", tint = AfricoOnDarkMuted)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = AfricoOnDarkMuted)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AfricoDarkSurface
        )
    )
}

@Composable
private fun AmountDisplay(displayAmount: String, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Montant à encaisser",
                color = AfricoOnDarkMuted,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = displayAmount,
                transitionSpec = {
                    slideInVertically { -it } togetherWith slideOutVertically { it }
                },
                label = "amount_anim"
            ) { amount ->
                Text(
                    text = amount,
                    color = AfricoOnDark,
                    fontSize = if (amount.length > 10) 28.sp else 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = currency,
                color = AfricoGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        label = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "⌫" -> onDelete()
                                "C"  -> onClear()
                                else -> onDigit(key)
                            }
                        },
                        isSpecial = key == "C" || key == "⌫"
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isSpecial: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSpecial) AfricoDarkElevated else AfricoDarkSurface,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSpecial) AfricoOnDarkMuted else AfricoOnDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Format a long amount in XOF centimes to a display string. */
fun formatXof(amountCents: Long): String {
    // XOF has no decimal places (currency subunit = 1)
    return "%,d".format(amountCents).replace(',', ' ') + " F"
}
