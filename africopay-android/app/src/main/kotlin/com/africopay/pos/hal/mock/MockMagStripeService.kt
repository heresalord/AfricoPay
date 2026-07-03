package com.africopay.pos.hal.mock

import com.africopay.pos.domain.model.*
import com.africopay.pos.hal.interfaces.MagStripeService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/** Mock Magnetic Stripe Reader for simulation mode. */
class MockMagStripeService @Inject constructor(
    private val simulationConfig: SimulationConfig
) : MagStripeService {

    override fun isAvailable(): Boolean = true

    override fun startReading(): Flow<MagStripeEvent> = flow {
        emit(MagStripeEvent.WaitingForSwipe)
        delay(simulationConfig.processingDelayMs)
        emit(MagStripeEvent.CardSwiped(MagStripeData(track1 = "SIMULATED", track2 = "0000000000")))
    }

    override suspend fun processCard(cardData: MagStripeData): PaymentResult {
        delay(simulationConfig.processingDelayMs)
        val status = simulationConfig.nextResult()
        return PaymentResult(
            status = status,
            transactionId = UUID.randomUUID().toString(),
            receiptNumber = "SIM-MSR-${System.currentTimeMillis()}",
            authCode = if (status == TransactionStatus.APPROVED) "AUTH${(100000..999999).random()}" else null
        )
    }
}
