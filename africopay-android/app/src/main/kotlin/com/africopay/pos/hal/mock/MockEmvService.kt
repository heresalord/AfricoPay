package com.africopay.pos.hal.mock

import com.africopay.pos.core.util.HardwareCapabilitiesDetector
import com.africopay.pos.domain.model.*
import com.africopay.pos.hal.interfaces.EmvService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * EMV chip reader HAL. [isAvailable] reflects real detection — no manufacturer SDK
 * (ZCS/Sunmi/PAX/Newland/MoreFun) is wired in yet, so this always reports unavailable
 * until one is integrated. The card-insertion simulation below only ever runs once a
 * real implementation reports the reader as present.
 */
class MockEmvService @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val hardwareDetector: HardwareCapabilitiesDetector
) : EmvService {

    override fun isAvailable(): Boolean = hardwareDetector.detect().hasEmv

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
