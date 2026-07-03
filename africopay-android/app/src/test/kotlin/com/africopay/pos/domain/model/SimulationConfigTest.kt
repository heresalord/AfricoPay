package com.africopay.pos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SimulationConfigTest {

    @Test
    fun testDefaultOutcome() {
        val config = SimulationConfig(defaultOutcome = TransactionStatus.APPROVED)
        val result = config.nextResult(10000L)
        assertEquals(TransactionStatus.APPROVED, result)
    }

    @Test
    fun testCustomOutcomeForAmount() {
        val customMap = mapOf(
            5000L to TransactionStatus.DECLINED,
            15000L to TransactionStatus.INSUFFICIENT_FUNDS
        )
        val config = SimulationConfig(
            defaultOutcome = TransactionStatus.APPROVED,
            customOutcomes = customMap
        )

        assertEquals(TransactionStatus.DECLINED, config.nextResult(5000L))
        assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, config.nextResult(15000L))
        assertEquals(TransactionStatus.APPROVED, config.nextResult(10000L))
    }
}
