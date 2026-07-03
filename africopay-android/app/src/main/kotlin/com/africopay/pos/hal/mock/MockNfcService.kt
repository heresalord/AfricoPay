package com.africopay.pos.hal.mock

import com.africopay.pos.domain.model.*
import com.africopay.pos.hal.interfaces.NfcService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * Mock NFC service for simulation mode (v0.1.0).
 * Simulates card detection and payment processing without real hardware.
 */
class MockNfcService @Inject constructor(
    private val simulationConfig: SimulationConfig
) : NfcService {

    override fun isAvailable(): Boolean = true
    override fun isEnabled(): Boolean = true

    override fun startListening(): Flow<NfcEvent> = flow {
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
