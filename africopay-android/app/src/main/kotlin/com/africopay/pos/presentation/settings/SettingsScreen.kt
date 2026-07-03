package com.africopay.pos.presentation.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.africopay.pos.presentation.theme.*

/**
 * Settings screen — Merchant profile configuration and application parameters.
 * Protected by merchant PIN in production.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var merchantName by remember { mutableStateOf("Boutique Demo") }
    var merchantId by remember { mutableStateOf("MERCH-12345") }
    var storeAddress by remember { mutableStateOf("Cotonou, Bénin") }
    var phone by remember { mutableStateOf("+229 00 00 00 00") }
    var receiptFooter by remember { mutableStateOf("Merci pour votre achat !") }
    var simulationMode by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = AfricoDark,
        topBar = {
            TopAppBar(
                title = { Text("Paramètres", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = AfricoOnDarkMuted)
                    }
                },
                actions = {
                    TextButton(onClick = { /* Save */ }) {
                        Text("Enregistrer", color = AfricoGreen, fontWeight = FontWeight.Bold)
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

            // Merchant Section
            SettingsSection(title = "Profil Marchand") {
                SettingsTextField("Nom du Marchand", merchantName, Icons.Default.Store) { merchantName = it }
                SettingsTextField("ID Marchand", merchantId, Icons.Default.Badge) { merchantId = it }
                SettingsTextField("Adresse Boutique", storeAddress, Icons.Default.LocationOn) { storeAddress = it }
                SettingsTextField("Téléphone", phone, Icons.Default.Phone) { phone = it }
                SettingsTextField("Pied de Reçu", receiptFooter, Icons.Default.Receipt) { receiptFooter = it }
            }

            // Application Section
            SettingsSection(title = "Application") {
                SettingsToggleRow(
                    label = "Mode Simulation",
                    description = "Transactions simulées (aucun paiement réel)",
                    icon = Icons.Default.Science,
                    checked = simulationMode,
                    onCheckedChange = { simulationMode = it }
                )
            }

            // Terminal Info Section
            SettingsSection(title = "Informations Terminal") {
                SettingsInfoRow("Version Application", "0.1.0", Icons.Default.Info)
                SettingsInfoRow("Version Base de Données", "1", Icons.Default.Storage)
                SettingsInfoRow("ID Terminal", "SIM-001", Icons.Default.PhoneAndroid)
                SettingsInfoRow("Devise", "XOF (Franc CFA BCEAO)", Icons.Default.AttachMoney)
                SettingsInfoRow("Langue", "Français", Icons.Default.Language)
            }

            // Security Section
            SettingsSection(title = "Sécurité") {
                SettingsActionRow("Modifier PIN Marchand", Icons.Default.Lock) { }
                SettingsActionRow("Modifier PIN Administrateur", Icons.Default.AdminPanelSettings) { }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), color = AfricoOnDarkMuted, fontSize = 11.sp,
            letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AfricoDarkSurface)
        ) {
            Column(modifier = Modifier.padding(4.dp), content = content)
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    icon: ImageVector,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = AfricoOnDarkMuted, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = AfricoGreen, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AfricoGreen,
            unfocusedBorderColor = AfricoDarkElevated,
            focusedTextColor = AfricoOnDark,
            unfocusedTextColor = AfricoOnDark,
            cursorColor = AfricoGreen,
            unfocusedContainerColor = AfricoDark,
            focusedContainerColor = AfricoDark
        )
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AfricoGold, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = AfricoOnDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(description, color = AfricoOnDarkMuted, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AfricoDark, checkedTrackColor = AfricoGreen)
        )
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AfricoOnDarkMuted, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = AfricoOnDark, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = AfricoOnDarkMuted, fontSize = 13.sp)
    }
}

@Composable
private fun SettingsActionRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AfricoError, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = AfricoError, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AfricoError, modifier = Modifier.size(18.dp))
    }
}
