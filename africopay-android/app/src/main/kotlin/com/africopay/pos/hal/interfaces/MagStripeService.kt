package com.africopay.pos.hal.interfaces

import com.africopay.pos.domain.model.MagStripeData
import com.africopay.pos.domain.model.MagStripeEvent
import com.africopay.pos.domain.model.PaymentResult
import kotlinx.coroutines.flow.Flow

/**
 * Hardware Abstraction Layer interface for Magnetic Stripe Reader (MSR).
 */
interface MagStripeService {
    /** Returns true if the magnetic stripe reader is physically present. */
    fun isAvailable(): Boolean

    /**
     * Starts listening for magnetic stripe card swipe events.
     * Emits [MagStripeEvent] when a card is swiped.
     */
    fun startReading(): Flow<MagStripeEvent>

    /**
     * Processes a swiped magnetic stripe card for payment.
     * @param cardData Data read from the magnetic stripe.
     * @return [PaymentResult] with approval status and details.
     */
    suspend fun processCard(cardData: MagStripeData): PaymentResult
}
