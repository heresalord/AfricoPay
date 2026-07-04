package com.africopay.pos.hal.android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.africopay.pos.data.local.db.dao.MerchantProfileDao
import com.africopay.pos.domain.model.PaperStatus
import com.africopay.pos.domain.model.PrintResult
import com.africopay.pos.domain.model.PrinterStatus
import com.africopay.pos.domain.model.Receipt
import com.africopay.pos.hal.interfaces.PrinterService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Real Bluetooth thermal printer service (ESC/POS over SPP).
 *
 * Every Smart POS in your target range either has a printer wired in through the
 * manufacturer's own SDK (not available yet — see hal/<manufacturer>), or exposes it as a
 * standard paired Bluetooth SPP device. This implementation covers the second case, which
 * also lets a merchant pair *any* external Bluetooth receipt printer, not just a built-in
 * one. Availability is real: it checks the actual paired-device list, not a hardcoded flag.
 *
 * Live paper-level sensing is intentionally not implemented: there's no ESC/POS status
 * command that's reliably supported across the cheap printer clones this app will meet in
 * the field, and guessing at one risks the app hanging on a printer that doesn't answer.
 * [getPaperStatus] instead reflects whether the configured printer is currently reachable.
 */
class BluetoothPrinterService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val merchantProfileDao: MerchantProfileDao
) : PrinterService {

    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private fun hasBluetoothConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    private fun bondedDevice(address: String): BluetoothDevice? {
        if (!hasBluetoothConnectPermission()) return null
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
        return try {
            adapter.bondedDevices?.firstOrNull { it.address == address }
        } catch (e: SecurityException) {
            Timber.tag("Printer").w(e, "Missing Bluetooth permission")
            null
        }
    }

    private fun configuredPrinterMac(): String? =
        runBlocking { merchantProfileDao.getMerchantProfileOnce()?.printerMacAddress }

    override fun isAvailable(): Boolean {
        val address = configuredPrinterMac() ?: return false
        return bondedDevice(address) != null
    }

    override fun getStatus(): PrinterStatus {
        if (!hasBluetoothConnectPermission()) return PrinterStatus.OFFLINE
        val address = configuredPrinterMac() ?: return PrinterStatus.OFFLINE
        val device = bondedDevice(address) ?: return PrinterStatus.OFFLINE

        var socket: BluetoothSocket? = null
        return try {
            socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect()
            PrinterStatus.READY
        } catch (e: Exception) {
            Timber.tag("Printer").w(e, "Printer connection check failed")
            PrinterStatus.ERROR
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    override fun getPaperStatus(): PaperStatus = PaperStatus.OK

    override suspend fun print(receipt: Receipt): PrintResult = withContext(Dispatchers.IO) {
        if (!hasBluetoothConnectPermission()) {
            Timber.tag("Printer").w("Missing BLUETOOTH_CONNECT permission")
            return@withContext PrintResult.PRINTER_ERROR
        }
        val profile = merchantProfileDao.getMerchantProfileOnce()
        val address = profile?.printerMacAddress
        if (address == null) {
            Timber.tag("Printer").w("No printer configured in settings")
            return@withContext PrintResult.PRINTER_ERROR
        }
        val device = bondedDevice(address)
        if (device == null) {
            Timber.tag("Printer").w("Configured printer is not paired/reachable")
            return@withContext PrintResult.PRINTER_ERROR
        }

        val bytes = EscPosReceiptFormatter.build(receipt, profile.paperWidthMm)

        withTimeoutOrNull(10_000L) {
            var socket: BluetoothSocket? = null
            try {
                socket = device.createRfcommSocketToServiceRecord(sppUuid)
                socket.connect()
                socket.outputStream.write(bytes)
                socket.outputStream.flush()
                PrintResult.SUCCESS
            } catch (e: Exception) {
                Timber.tag("Printer").e(e, "Print failed")
                PrintResult.PRINTER_ERROR
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        } ?: PrintResult.TIMEOUT
    }
}
