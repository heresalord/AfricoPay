package com.africopay.pos.hal.mock

import com.africopay.pos.domain.model.*
import com.africopay.pos.hal.interfaces.EmvService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/** Mock EMV chip card reader for simulation mode. */
class MockEmvService @Inject constructor(
    private val simulationConfig: SimulationConfig
) : EmvService {

    override fun isAvailable(): Boolean = true

    override fun startReading(): Flow<EmvEvent> = flow {
        emit(EmvEvent.WaitingForCard)
        delay(simulationConfig.processingDelayMs)
        emit(EmvEvent.CardInserted(EmvCardData()))
    }

    override suspend fun processCard(cardData: EmvCardData): PaymentResult {
        delay(simulationConfig.processingDelayMs)
        val status = simulationConfig.nextResult()
        return PaymentResult(
            status = status,
            transactionId = UUID.randomUUID().toString(),
            receiptNumber = "SIM-EMV-${System.currentTimeMillis()}",
            authCode = if (status == TransactionStatus.APPROVED) "AUTH${(100000..999999).random()}" else null
        )
    }

    override fun cancelReading() { /* No-op in simulation */ }
}
