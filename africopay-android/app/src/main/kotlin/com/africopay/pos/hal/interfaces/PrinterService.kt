package com.africopay.pos.hal.interfaces

import com.africopay.pos.domain.model.Receipt
import com.africopay.pos.domain.model.PrintResult
import com.africopay.pos.domain.model.PrinterStatus
import com.africopay.pos.domain.model.PaperStatus

/**
 * Hardware Abstraction Layer interface for thermal printer.
 * All manufacturer-specific implementations must implement this interface.
 * The UI and domain layers must never depend on concrete implementations.
 */
interface PrinterService {
    /** Returns true if the printer hardware is physically present on this device. */
    fun isAvailable(): Boolean

    /** Returns the current printer status (READY, ERROR, OFFLINE, etc.). */
    fun getStatus(): PrinterStatus

    /** Returns the current paper status (OK, LOW, EMPTY). */
    fun getPaperStatus(): PaperStatus

    /** Prints a receipt. Suspends until print job completes or fails. */
    suspend fun print(receipt: Receipt): PrintResult
}
