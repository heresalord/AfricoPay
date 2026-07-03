package com.africopay.pos.core.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Generates real, scannable QR code bitmaps (ZXing) for the QR payment flow. */
object QrCodeGenerator {

    fun generate(content: String, sizePx: Int = 720): Bitmap {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bitmap
    }

    /** Builds the payload encoded into the QR — a merchant reference the customer's app would parse. */
    fun buildPayload(merchantId: String, terminalId: String, amountCents: Long, reference: String): String =
        "africopay://pay?merchant=$merchantId&terminal=$terminalId&amount=$amountCents&ref=$reference"
}
