package com.africopay.pos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SimulationConfigTest {

    @Test
    fun testDefaultOutcomeIsApproved() {
        val config = SimulationConfig()
        val outcome = config.nextResult(1000L)
        assertEquals(TransactionStatus.APPROVED, outcome)
    }

    @Test
    fun testCustomOutcomeForSpecificAmount() {
        val config = SimulationConfig(
            defaultOutcome = TransactionStatus.APPROVED,
            customOutcomes = mapOf(5000L to TransactionStatus.DECLINED)
        )
        assertEquals(TransactionStatus.DECLINED, config.nextResult(5000L))
        assertEquals(TransactionStatus.APPROVED, config.nextResult(1000L))
    }
}
