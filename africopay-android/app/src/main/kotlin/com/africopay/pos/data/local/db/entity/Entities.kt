package com.africopay.pos.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val serverId: String? = null,
    val amount: Long,
    val currency: String = "XOF",
    val paymentMethod: String,
    val mobileProvider: String? = null,
    val status: String,
    val declineReason: String? = null,
    val timestamp: Long,
    val receiptNumber: String,
    val terminalId: String,
    val merchantId: String,
    val isSimulated: Boolean = true,
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val receiptNumber: String,
    val merchantName: String,
    val merchantAddress: String? = null,
    val merchantPhone: String? = null,
    val merchantId: String,
    val terminalId: String,
    val paymentMethod: String,
    val amount: Long,
    val currency: String = "XOF",
    val status: String,
    val dateTime: Long,
    val footerText: String? = null,
    val isSimulated: Boolean = true,
    val printedCount: Int = 0,
    val lastPrintedAt: Long? = null,
    val createdAt: Long
)

@Entity(tableName = "merchant_profile")
data class MerchantProfileEntity(
    @PrimaryKey val id: String = "SINGLETON",
    val merchantName: String = "",
    val merchantId: String = "",
    val storeAddress: String = "",
    val phone: String = "",
    val currency: String = "XOF",
    val language: String = "fr",
    val receiptFooter: String = "Merci pour votre achat !",
    val simulationMode: Boolean = true,
    val adminPinHash: String = "",
    val merchantPinHash: String = "",
    val terminalId: String = "",
    val appVersion: String = "0.1.0",
    val dbVersion: Int = 1,
    val updatedAt: Long
)

@Entity(tableName = "hardware_diagnostics")
data class HardwareDiagnosticEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val androidVersion: Int,
    val manufacturer: String,
    val model: String,
    val cpu: String? = null,
    val ramMb: Int? = null,
    val storageGb: Int? = null,
    val batteryLevel: Int? = null,
    val batteryCharging: Boolean = false,
    val hasNfc: Boolean = false,
    val nfcEnabled: Boolean = false,
    val hasEmv: Boolean = false,
    val emvDriverVersion: String? = null,
    val hasMagStripe: Boolean = false,
    val hasPrinter: Boolean = false,
    val printerPaperOk: Boolean = false,
    val hasCamera: Boolean = false,
    val hasScanner: Boolean = false,
    val hasGps: Boolean = false,
    val hasBluetooth: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val hasWifi: Boolean = false,
    val wifiConnected: Boolean = false,
    val networkType: String? = null,
    val signalStrength: Int? = null,
    val createdAt: Long
)
