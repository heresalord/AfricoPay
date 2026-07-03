package com.africopay.pos.hal.interfaces

import android.app.Activity
import com.africopay.pos.domain.model.NfcCardData
import com.africopay.pos.domain.model.NfcEvent
import com.africopay.pos.domain.model.PaymentResult
import kotlinx.coroutines.flow.Flow

/**
 * Hardware Abstraction Layer interface for NFC contactless payment.
 * All manufacturer-specific implementations must implement this interface.
 */
interface NfcService {
    /** Returns true if NFC hardware is physically present. */
    fun isAvailable(): Boolean

    /** Returns true if NFC is enabled in Android settings. */
    fun isEnabled(): Boolean

    /**
     * Starts listening for NFC card/tag events using reader mode on [activity].
     * Emits [NfcEvent] when a card is detected or an error occurs. Reader mode is
     * disabled automatically when the collecting coroutine is cancelled.
     */
    fun startListening(activity: Activity): Flow<NfcEvent>

    /** Stops listening for NFC events. */
    fun stopListening()

    /**
     * Processes a detected NFC card for payment.
     * @param cardData Data captured from the NFC card.
     * @return [PaymentResult] with approval status and details.
     */
    suspend fun processCard(cardData: NfcCardData): PaymentResult
}
