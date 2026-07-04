package com.africopay.pos.domain.model

import java.time.Instant

// ─── Payment & Transaction ────────────────────────────────────────────────────

enum class PaymentMethod(val displayName: String) {
    NFC("Carte Sans Contact"),
    EMV("Carte à Puce"),
    MAG_STRIPE("Bande Magnétique"),
    QR_CODE("QR Code"),
    MOBILE_MONEY("Mobile Money"),
    CASH("Espèces")
}

enum class MobileMoneyProvider(val displayName: String) {
    MTN("MTN Mobile Money"),
    MOOV("Moov Money"),
    ORANGE("Orange Money"),
    WAVE("Wave")
}

enum class TransactionStatus {
    APPROVED,
    DECLINED,
    TIMEOUT,
    NETWORK_ERROR,
    CANCELLED,
    CARD_EXPIRED,
    INSUFFICIENT_FUNDS,
    ISSUER_OFFLINE,
    UNKNOWN_ERROR
}

data class Transaction(
    val id: String,
    val serverId: String? = null,
    val amount: Long,                       // In XOF centimes
    val currency: String = "XOF",
    val paymentMethod: PaymentMethod,
    val mobileProvider: MobileMoneyProvider? = null,
    val status: TransactionStatus,
    val declineReason: String? = null,
    val timestamp: Instant,
    val receiptNumber: String,
    val terminalId: String,
    val merchantId: String,
    val isSimulated: Boolean = true,
    val isSynced: Boolean = false
)

data class PaymentResult(
    val status: TransactionStatus,
    val transactionId: String,
    val receiptNumber: String,
    val authCode: String? = null,
    val declineReason: String? = null,
    val processingTimeMs: Long = 0
)

// ─── Hardware ─────────────────────────────────────────────────────────────────

data class HardwareCapabilities(
    val hasNfc: Boolean = false,
    val nfcEnabled: Boolean = false,
    val hasEmv: Boolean = false,
    val hasMagStripe: Boolean = false,
    val hasCamera: Boolean = false,
    val hasScanner: Boolean = false,
    val hasPrinter: Boolean = false,
    val hasGps: Boolean = false,
    val hasBluetooth: Boolean = false,
    val hasWifi: Boolean = false,
    val androidVersion: Int = 0,
    val manufacturer: String = "",
    val model: String = "",
    val cpu: String = "",
    val ramMb: Int = 0,
    val storageGb: Int = 0,
    val batteryLevel: Int = 0,
    val batteryCharging: Boolean = false,
    val networkType: String = "NONE"
)

data class HardwareComponent(
    val name: String,
    val isAvailable: Boolean,
    val version: String? = null,
    val driverInfo: String? = null
)

// ─── Printer ──────────────────────────────────────────────────────────────────

enum class PrinterStatus { READY, ERROR, OFFLINE, BUSY }
enum class PaperStatus { OK, LOW, EMPTY }
enum class PrintResult { SUCCESS, PAPER_EMPTY, PRINTER_ERROR, TIMEOUT }

data class Receipt(
    val id: String,
    val transactionId: String,
    val receiptNumber: String,
    val merchantName: String,
    val merchantAddress: String? = null,
    val merchantPhone: String? = null,
    val merchantId: String,
    val terminalId: String,
    val paymentMethod: PaymentMethod,
    val amount: Long,
    val currency: String = "XOF",
    val status: TransactionStatus,
    val dateTime: Instant,
    val footerText: String? = null,
    val isSimulated: Boolean = true
)

// ─── NFC Events ───────────────────────────────────────────────────────────────

sealed class NfcEvent {
    object Listening : NfcEvent()
    data class CardDetected(val cardData: NfcCardData) : NfcEvent()
    data class Error(val message: String) : NfcEvent()
    object Removed : NfcEvent()
}

data class NfcCardData(val rawData: ByteArray = byteArrayOf())

// ─── EMV Events ───────────────────────────────────────────────────────────────

sealed class EmvEvent {
    object WaitingForCard : EmvEvent()
    data class CardInserted(val cardData: EmvCardData) : EmvEvent()
    object CardRemoved : EmvEvent()
    data class Error(val message: String) : EmvEvent()
}

data class EmvCardData(val rawData: ByteArray = byteArrayOf())

// ─── Mag Stripe Events ────────────────────────────────────────────────────────

sealed class MagStripeEvent {
    object WaitingForSwipe : MagStripeEvent()
    data class CardSwiped(val cardData: MagStripeData) : MagStripeEvent()
    data class Error(val message: String) : MagStripeEvent()
}

data class MagStripeData(val track1: String = "", val track2: String = "")

// ─── Scanner / QR ─────────────────────────────────────────────────────────────

data class ScanResult(val barcode: String, val format: String)
data class QrResult(val content: String)

// ─── Merchant ─────────────────────────────────────────────────────────────────

data class MerchantProfile(
    val merchantName: String = "",
    val merchantId: String = "",
    val storeAddress: String = "",
    val phone: String = "",
    val currency: String = "XOF",
    val language: String = "fr",
    val receiptFooter: String = "Merci pour votre achat !",
    val simulationMode: Boolean = true,
    val terminalId: String = "",
    val appVersion: String = "0.1.0",
    val dbVersion: Int = 1,
    val printerMacAddress: String? = null,
    val printerName: String? = null,
    val paperWidthMm: Int = 58
)

// ─── Simulation Config ────────────────────────────────────────────────────────

data class SimulationConfig(
    val processingDelayMs: Long = 2000L,
    val defaultOutcome: TransactionStatus = TransactionStatus.APPROVED,
    val customOutcomes: Map<Long, TransactionStatus> = emptyMap()
) {
    /** Returns the next simulated result based on configuration. */
    fun nextResult(amount: Long = 0): TransactionStatus =
        customOutcomes[amount] ?: defaultOutcome
}
