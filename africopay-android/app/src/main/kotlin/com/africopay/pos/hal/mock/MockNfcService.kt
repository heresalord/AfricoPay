package com.africopay.pos.hal.mock

import android.app.Activity
import com.africopay.pos.domain.model.*
import com.africopay.pos.hal.interfaces.NfcService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * Fully-simulated NFC service (no real hardware event, fixed delay then a fake tap).
 * Used only for unit tests / non-Activity contexts. The app itself uses
 * [com.africopay.pos.hal.android.AndroidNfcService] for real tap detection.
 */
class MockNfcService @Inject constructor(
    private val simulationConfig: SimulationConfig
) : NfcService {

    override fun isAvailable(): Boolean = true
    override fun isEnabled(): Boolean = true

    override fun startListening(activity: Activity): Flow<NfcEvent> = flow {
        emit(NfcEvent.Listening)
        delay(simulationConfig.processingDelayMs)
        emit(NfcEvent.CardDetected(NfcCardData()))
    }

    override fun stopListening() {
        // No-op in simulation
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
