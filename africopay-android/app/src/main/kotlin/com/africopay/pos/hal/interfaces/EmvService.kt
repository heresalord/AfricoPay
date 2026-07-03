package com.africopay.pos.hal.interfaces

import com.africopay.pos.domain.model.EmvCardData
import com.africopay.pos.domain.model.EmvEvent
import com.africopay.pos.domain.model.PaymentResult
import kotlinx.coroutines.flow.Flow

/**
 * Hardware Abstraction Layer interface for EMV chip card reader.
 */
interface EmvService {
    /** Returns true if an EMV chip card reader is physically present. */
    fun isAvailable(): Boolean

    /**
     * Starts listening for EMV card insertion/removal events.
     * Emits [EmvEvent] on state changes.
     */
    fun startReading(): Flow<EmvEvent>

    /**
     * Processes an inserted EMV chip card for payment.
     * @param cardData Data read from the EMV chip.
     * @return [PaymentResult] with approval status and details.
     */
    suspend fun processCard(cardData: EmvCardData): PaymentResult

    /** Cancels current EMV reading session. */
    fun cancelReading()
}
