package com.africopay.pos.hal.mock

import com.africopay.pos.domain.model.*
import com.africopay.pos.hal.interfaces.PrinterService
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Mock Thermal Printer for simulation mode. */
class MockPrinterService @Inject constructor() : PrinterService {

    override fun isAvailable(): Boolean = true
    override fun getStatus(): PrinterStatus = PrinterStatus.READY
    override fun getPaperStatus(): PaperStatus = PaperStatus.OK

    override suspend fun print(receipt: Receipt): PrintResult {
        delay(1500L) // Simulate print time
        return PrintResult.SUCCESS
    }
}
