package com.africopay.pos.hal.android

import com.africopay.pos.domain.model.Receipt
import com.africopay.pos.domain.model.TransactionStatus
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds real ESC/POS byte commands for thermal receipt printers.
 * Line width adapts to the configured paper size: 58mm ≈ 32 columns,
 * 80mm ≈ 48 columns at the printer's default (Font A, 12x24) character size.
 */
object EscPosReceiptFormatter {

    // Cheap BT thermal printers vary wildly in which code page their firmware maps to a
    // given byte value, and Android doesn't reliably ship legacy code pages like CP850/CP437
    // anyway. Rather than gamble on a code page match we don't know per hardware model,
    // text is transliterated to plain ASCII before printing — guaranteed to render correctly
    // on any ESC/POS printer's default table, at the cost of accents (é -> e, etc).
    private val CHARSET: Charset = Charset.forName("US-ASCII")

    private fun sanitize(s: String): String {
        val normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{M}"), "")
            .replace('œ', 'o').replace('Œ', 'O')
            .replace(Regex("[^\\x00-\\x7F]"), "?")
    }

    private const val ESC = 0x1B
    private const val GS = 0x1D

    fun columnsForPaperWidth(paperWidthMm: Int): Int = if (paperWidthMm >= 80) 48 else 32

    fun build(receipt: Receipt, paperWidthMm: Int): ByteArray {
        val cols = columnsForPaperWidth(paperWidthMm)
        val out = ByteArrayOutputStream()

        fun raw(vararg bytes: Int) = out.write(bytes.map { it.toByte() }.toByteArray())
        fun text(s: String) = out.write(sanitize(s).toByteArray(CHARSET))
        fun line(s: String = "") { text(s); raw(0x0A) }
        fun center() = raw(ESC, 0x61, 1)
        fun left() = raw(ESC, 0x61, 0)
        fun boldOn() = raw(ESC, 0x45, 1)
        fun boldOff() = raw(ESC, 0x45, 0)
        fun doubleSize() = raw(GS, 0x21, 0x11)
        fun normalSize() = raw(GS, 0x21, 0x00)
        fun divider() = line("-".repeat(cols))
        fun row(label: String, value: String) {
            val space = (cols - label.length - value.length).coerceAtLeast(1)
            line(label + " ".repeat(space) + value)
        }

        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.FRANCE)
        val date = Date.from(receipt.dateTime)

        raw(ESC, 0x40) // Initialize printer

        if (receipt.isSimulated) {
            center(); boldOn()
            line("* TRANSACTION TEST *")
            boldOff()
        }

        center(); boldOn()
        line(receipt.merchantName.ifBlank { "AFRICOPAY POS" })
        boldOff()
        if (!receipt.merchantAddress.isNullOrBlank()) line(receipt.merchantAddress)
        if (!receipt.merchantPhone.isNullOrBlank()) line(receipt.merchantPhone)

        left()
        divider()
        row("Date", dateFmt.format(date))
        row("Heure", timeFmt.format(date))
        row("N. Recu", receipt.receiptNumber)
        row("Terminal", receipt.terminalId)
        row("Marchand", receipt.merchantId)
        row("Methode", receipt.paymentMethod.displayName)
        divider()

        center()
        line("MONTANT")
        doubleSize(); boldOn()
        line(formatXofPlain(receipt.amount))
        normalSize(); boldOff()
        divider()

        val statusLabel = if (receipt.status == TransactionStatus.APPROVED) "APPROUVE" else statusLabelFr(receipt.status)
        boldOn()
        line(statusLabel)
        boldOff()
        divider()

        if (!receipt.footerText.isNullOrBlank()) {
            line(receipt.footerText)
        }
        line("www.africopay.com")
        line()
        line()

        raw(GS, 0x56, 0x01) // Partial cut

        return out.toByteArray()
    }

    private fun formatXofPlain(amountCents: Long): String =
        "%,d".format(amountCents).replace(',', ' ') + " F CFA"

    private fun statusLabelFr(status: TransactionStatus): String = when (status) {
        TransactionStatus.APPROVED -> "APPROUVE"
        TransactionStatus.DECLINED -> "REFUSE"
        TransactionStatus.TIMEOUT -> "DELAI DEPASSE"
        TransactionStatus.NETWORK_ERROR -> "ERREUR RESEAU"
        TransactionStatus.CANCELLED -> "ANNULE"
        TransactionStatus.CARD_EXPIRED -> "CARTE EXPIREE"
        TransactionStatus.INSUFFICIENT_FUNDS -> "FONDS INSUFFISANTS"
        TransactionStatus.ISSUER_OFFLINE -> "EMETTEUR HORS LIGNE"
        TransactionStatus.UNKNOWN_ERROR -> "ERREUR INCONNUE"
    }
}
