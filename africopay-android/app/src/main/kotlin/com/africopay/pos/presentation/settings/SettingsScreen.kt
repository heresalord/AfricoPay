package com.africopay.pos.presentation.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.africopay.pos.BuildConfig
import com.africopay.pos.data.local.db.dao.MerchantProfileDao
import com.africopay.pos.data.local.db.entity.MerchantProfileEntity
import com.africopay.pos.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val merchantName: String = "",
    val merchantId: String = "",
    val storeAddress: String = "",
    val phone: String = "",
    val receiptFooter: String = "Merci pour votre achat !",
    val simulationMode: Boolean = true,
    val terminalId: String = "",
    val printerMacAddress: String? = null,
    val printerName: String? = null,
    val paperWidthMm: Int = 58,
    val isLoaded: Boolean = false,
    val savedAt: Long? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val merchantProfileDao: MerchantProfileDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = merchantProfileDao.getMerchantProfileOnce()
            if (profile != null) {
                _uiState.value = SettingsUiState(
                    merchantName = profile.merchantName,
                    merchantId = profile.merchantId,
                    storeAddress = profile.storeAddress,
                    phone = profile.phone,
                    receiptFooter = profile.receiptFooter,
                    simulationMode = profile.simulationMode,
                    terminalId = profile.terminalId,
                    printerMacAddress = profile.printerMacAddress,
                    printerName = profile.printerName,
                    paperWidthMm = profile.paperWidthMm,
                    isLoaded = true
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoaded = true)
            }
        }
    }

    fun update(transform: (SettingsUiState) -> SettingsUiState) {
        _uiState.value = transform(_uiState.value)
    }

    fun save() {
        viewModelScope.launch {
            val s = _uiState.value
            merchantProfileDao.upsertProfile(
                MerchantProfileEntity(
                    merchantName = s.merchantName,
                    merchantId = s.merchantId,
                    storeAddress = s.storeAddress,
                    phone = s.phone,
                    receiptFooter = s.receiptFooter,
                    simulationMode = s.simulationMode,
                    terminalId = s.terminalId,
                    printerMacAddress = s.printerMacAddress,
                    printerName = s.printerName,
                    paperWidthMm = s.paperWidthMm,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _uiState.value = s.copy(savedAt = System.currentTimeMillis())
        }
    }
}

/**
 * Settings screen — Merchant profile configuration, printer pairing and paper width,
 * all persisted to Room. Protected by merchant PIN in production.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.savedAt) {
        if (state.savedAt != null) {
            snackbarHostState.showSnackbar("Paramètres enregistrés")
        }
    }

    Scaffold(
        containerColor = AfricoDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Paramètres", color = AfricoOnDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = AfricoOnDarkMuted)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save() }) {
                        Text("Enregistrer", color = AfricoGreen, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AfricoDarkSurface)
            )
        }
    ) { padding ->
        if (!state.isLoaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AfricoGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Profil Marchand") {
                SettingsTextField("Nom du Marchand", state.merchantName, Icons.Default.Store) {
                    viewModel.update { s -> s.copy(merchantName = it) }
                }
                SettingsTextField("ID Marchand", state.merchantId, Icons.Default.Badge) {
                    viewModel.update { s -> s.copy(merchantId = it) }
                }
                SettingsTextField("Adresse Boutique", state.storeAddress, Icons.Default.LocationOn) {
                    viewModel.update { s -> s.copy(storeAddress = it) }
                }
                SettingsTextField("Téléphone", state.phone, Icons.Default.Phone) {
                    viewModel.update { s -> s.copy(phone = it) }
                }
                SettingsTextField("Pied de Reçu", state.receiptFooter, Icons.Default.Receipt) {
                    viewModel.update { s -> s.copy(receiptFooter = it) }
                }
                SettingsTextField("ID Terminal", state.terminalId, Icons.Default.PhoneAndroid) {
                    viewModel.update { s -> s.copy(terminalId = it) }
                }
            }

            PrinterSection(
                state = state,
                onPrinterSelected = { address, name ->
                    viewModel.update { s -> s.copy(printerMacAddress = address, printerName = name) }
                },
                onPaperWidthSelected = { width ->
                    viewModel.update { s -> s.copy(paperWidthMm = width) }
                }
            )

            SettingsSection(title = "Application") {
                SettingsToggleRow(
                    label = "Mode Simulation",
                    description = "Marque les reçus comme des transactions test",
                    icon = Icons.Default.Science,
                    checked = state.simulationMode,
                    onCheckedChange = { checked -> viewModel.update { s -> s.copy(simulationMode = checked) } }
                )
            }

            SettingsSection(title = "Informations Terminal") {
                SettingsInfoRow("Version Application", BuildConfig.VERSION_NAME, Icons.Default.Info)
                SettingsInfoRow("Devise", "XOF (Franc CFA BCEAO)", Icons.Default.AttachMoney)
                SettingsInfoRow("Langue", "Français", Icons.Default.Language)
            }

            SettingsSection(title = "Sécurité") {
                SettingsActionRow("Modifier PIN Marchand", Icons.Default.Lock) { }
                SettingsActionRow("Modifier PIN Administrateur", Icons.Default.AdminPanelSettings) { }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrinterSection(
    state: SettingsUiState,
    onPrinterSelected: (address: String?, name: String?) -> Unit,
    onPaperWidthSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        )
    }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var showPicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) {
            pairedDevices = try {
                BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.toList() ?: emptyList()
            } catch (e: SecurityException) {
                emptyList()
            }
            showPicker = true
        }
    }

    fun openPicker() {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            pairedDevices = try {
                BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.toList() ?: emptyList()
            } catch (e: SecurityException) {
                emptyList()
            }
            showPicker = true
        }
    }

    SettingsSection(title = "Imprimante") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openPicker() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Print, contentDescription = null, tint = AfricoGreen, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Imprimante Bluetooth", color = AfricoOnDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    state.printerName ?: "Aucune imprimante associée — appuyez pour choisir",
                    color = if (state.printerName != null) AfricoSuccess else AfricoOnDarkMuted,
                    fontSize = 12.sp
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AfricoOnDarkMuted, modifier = Modifier.size(18.dp))
        }

        Divider(color = AfricoDark, thickness = 1.dp)

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text("Largeur du Papier", color = AfricoOnDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaperWidthChip("58mm", selected = state.paperWidthMm == 58) { onPaperWidthSelected(58) }
                PaperWidthChip("80mm", selected = state.paperWidthMm == 80) { onPaperWidthSelected(80) }
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = AfricoDarkSurface,
            title = { Text("Choisir une imprimante", color = AfricoOnDark) },
            text = {
                Column {
                    if (pairedDevices.isEmpty()) {
                        Text(
                            "Aucun appareil Bluetooth associé. Associez d'abord l'imprimante dans les paramètres Bluetooth du système, puis revenez ici.",
                            color = AfricoOnDarkMuted, fontSize = 13.sp
                        )
                    } else {
                        pairedDevices.forEach { device ->
                            val name = try { device.name ?: device.address } catch (e: SecurityException) { device.address }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPrinterSelected(device.address, name)
                                        showPicker = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = AfricoGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(name, color = AfricoOnDark, fontSize = 14.sp)
                                    Text(device.address, color = AfricoOnDarkMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Fermer", color = AfricoGreen)
                }
            }
        )
    }
}

@Composable
private fun PaperWidthChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) AfricoGreen.copy(alpha = 0.18f) else AfricoDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) AfricoGreen else AfricoOnDarkMuted.copy(alpha = 0.3f))
    ) {
        Text(
            label,
            color = if (selected) AfricoGreen else AfricoOnDarkMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
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
