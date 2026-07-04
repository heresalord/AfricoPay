package com.africopay.pos.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AfricoDark, AfricoDark, Color(0xFF0E1720))
                    )
                )
        ) {
            // Soft ambient glow behind the amount card — a small touch that reads as "designed".
            Box(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .size(280.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AfricoGreen.copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                AmountDisplay(displayAmount = displayAmount, currency = "XOF")

                Spacer(modifier = Modifier.height(28.dp))

                NumericKeypad(
                    onDigit = ::appendDigit,
                    onDelete = ::deleteDigit,
                    onClear = { amountStr = "" }
                )

                Spacer(modifier = Modifier.weight(1f))

                val hasAmount = (amountStr.toLongOrNull() ?: 0L) > 0

                Button(
                    onClick = ::onContinuePressed,
                    enabled = hasAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .then(
                            if (hasAmount)
                                Modifier.shadow(16.dp, RoundedCornerShape(18.dp), spotColor = AfricoGreen, ambientColor = AfricoGreen)
                            else Modifier
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AfricoGreen,
                        contentColor = Color(0xFF002014),
                        disabledContainerColor = AfricoDarkElevated,
                        disabledContentColor = AfricoOnDarkMuted
                    )
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONTINUER", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                TextButton(
                    onClick = { amountStr = "" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 2.dp)
                ) {
                    Text("ANNULER", color = AfricoError, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Africo", color = AfricoOnDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Pay", color = AfricoGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AfricoGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "SIMULATION",
                        color = AfricoGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AmountDisplay(displayAmount: String, currency: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(24.dp),
        color = AfricoDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MONTANT À ENCAISSER",
                color = AfricoOnDarkMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedContent(
                targetState = displayAmount,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "amount_anim"
            ) { amount ->
                Text(
                    text = amount,
                    color = AfricoOnDark,
                    fontSize = if (amount.length > 10) 30.sp else 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currency,
                color = AfricoGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        label = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "⌫" -> onDelete()
                                "C" -> onClear()
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "key_scale")

    Surface(
        modifier = modifier
            .height(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = if (isSpecial) AfricoDarkElevated else AfricoDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
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
