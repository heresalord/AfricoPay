package com.africopay.pos.hal.android

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.os.Bundle
import com.africopay.pos.domain.model.NfcCardData
import com.africopay.pos.domain.model.NfcEvent
import com.africopay.pos.domain.model.PaymentResult
import com.africopay.pos.domain.model.SimulationConfig
import com.africopay.pos.domain.model.TransactionStatus
import com.africopay.pos.hal.interfaces.NfcService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Real NFC service backed by Android's own [NfcAdapter] reader mode — works on any
 * NFC-capable phone, not just certified Smart POS hardware. Card presentation is real;
 * only the bank approval [processCard] result is simulated until a payment processor
 * is integrated (per PRD section 13).
 */
class AndroidNfcService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val simulationConfig: SimulationConfig
) : NfcService {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    override fun isAvailable(): Boolean = nfcAdapter != null

    override fun isEnabled(): Boolean = nfcAdapter?.isEnabled == true

    override fun startListening(activity: Activity): Flow<NfcEvent> = callbackFlow {
        val adapter = nfcAdapter
        if (adapter == null) {
            trySend(NfcEvent.Error("NFC non disponible sur cet appareil"))
            close()
            return@callbackFlow
        }
        if (!adapter.isEnabled) {
            trySend(NfcEvent.Error("NFC désactivé — activez-le dans les paramètres du téléphone"))
            close()
            return@callbackFlow
        }

        trySend(NfcEvent.Listening)

        val callback = NfcAdapter.ReaderCallback { tag ->
            Timber.tag("NFC").d("Tag detected: techList=${tag.techList.joinToString()}")
            trySend(NfcEvent.CardDetected(NfcCardData(rawData = tag.id ?: byteArrayOf())))
        }

        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }
        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        adapter.enableReaderMode(activity, callback, flags, options)

        awaitClose {
            adapter.disableReaderMode(activity)
        }
    }

    override fun stopListening() {
        // Reader mode is disabled via awaitClose when the Flow collector is cancelled.
        // Kept for interface parity / explicit external cancellation if ever needed.
    }

    override suspend fun processCard(cardData: NfcCardData): PaymentResult {
        delay(simulationConfig.processingDelayMs)
        val status = simulationConfig.nextResult()
        return PaymentResult(
            status = status,
            transactionId = UUID.randomUUID().toString(),
            receiptNumber = "SIM-NFC-${System.currentTimeMillis()}",
            authCode = if (status == TransactionStatus.APPROVED) "AUTH${(100000..999999).random()}" else null
        )
    }
}
