package com.africopay.pos.core.util

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.NfcAdapter
import android.os.BatteryManager
import android.os.Build
import com.africopay.pos.domain.model.HardwareCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads actual device/terminal hardware capabilities via Android system APIs.
 *
 * NFC and camera/QR are detected for real — that hardware exists on any commodity
 * Android device. EMV chip reader and magnetic stripe reader are physical Smart POS
 * peripherals that require a manufacturer SDK (ZCS, Sunmi, PAX, Newland, MoreFun —
 * see hal/<manufacturer>). None of those are wired in yet, so those two always report
 * unavailable here; callers decide whether to still offer them in simulation mode.
 */
@Singleton
class HardwareCapabilitiesDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun detect(): HardwareCapabilities {
        val pm = context.packageManager
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkCaps = connectivity?.activeNetwork?.let { connectivity.getNetworkCapabilities(it) }
        val networkType = when {
            networkCaps == null -> "NONE"
            networkCaps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            networkCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            networkCaps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "NONE"
        }
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

        return HardwareCapabilities(
            hasNfc = pm.hasSystemFeature(PackageManager.FEATURE_NFC),
            nfcEnabled = nfcAdapter?.isEnabled == true,
            hasEmv = false,        // no manufacturer HAL wired in yet
            hasMagStripe = false,  // no manufacturer HAL wired in yet
            hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
            hasScanner = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
            hasPrinter = false,    // no manufacturer HAL wired in yet
            hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            hasBluetooth = bluetoothAdapter != null,
            hasWifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI),
            androidVersion = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            cpu = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            ramMb = 0,
            storageGb = 0,
            batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1,
            batteryCharging = batteryManager?.isCharging ?: false,
            networkType = networkType
        )
    }
}
